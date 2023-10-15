(ns ubinote.migration
  "This migration should works for both h2 and postgres"
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [next.jdbc :as jdbc]
   [toucan2.core :as tc]
   [ubinote.config :as cfg]
   [ubinote.server.db :as db]))

(def migrations (atom []))

(def postgres? (= :postgres (cfg/config-kw :db-type)))

(defn- create-migrations-table-if-needed! []
  (log/info "Creating migration table if needed...")
  (tc/with-connection [conn]
    (with-open [stmt (jdbc/prepare conn ["CREATE TABLE IF NOT EXISTS migration (
                                         name VARCHAR PRIMARY KEY NOT NULL,
                                         created_at TIMESTAMP NOT NULL DEFAULT now());"])]
      (jdbc/execute! stmt))))

(defn- previous-migrations []
  (or (tc/select-fn-set :name :m/migration) #{}))

(defn- migrate!*
  "Runs DB migrations. Copied from metastore."
  []
  (log/info "Running migrations if needed...")
  (create-migrations-table-if-needed!)
  (let [the-previous-migrations (previous-migrations)]
    (doseq [[migration-name statement-or-map] @migrations]
      (when-not (the-previous-migrations migration-name)
        (tc/with-transaction [conn]
          (log/info "Running migration" migration-name)
          (try
           (with-open [stmt (jdbc/prepare conn [(if (map? statement-or-map)
                                                  ((cfg/config-kw :db-type) statement-or-map)
                                                  statement-or-map)])]
             (jdbc/execute! stmt)
             (tc/insert! :conn conn :m/migration :name migration-name))
           (catch Exception e
             (throw (ex-info (format "Data migration %s failed: %s" migration-name (.getMessage e)) (or (ex-data e) {})))))))))
  (log/info "Migrations finished"))

(def ^{:doc "Memoized migrate!* to only run once."} migrate!
  (memoize migrate!*))

(defn- defmigration* [migration-name sql-statement-or-map]
  (if (some #(= (str migration-name) (first %)) @migrations)
    (throw (ex-info (format "Migration with name %s existed" migration-name) {:migration-name migration-name}))
    (swap! migrations conj [migration-name sql-statement-or-map]))
  nil)

(defmacro ^:private defmigration
  "Define a migration, the sql-statement-or-map could be a string,
  or a map from db-type to SQL in case we need custom migration for each dbms."
  [migration-name sql-statement-or-map]
  (when (map? sql-statement-or-map)
    (assert (= db/supported-dbms (set (keys sql-statement-or-map))) "Make sure you define migration for all supported dbms"))
  `(defmigration* ~(str migration-name) ~sql-statement-or-map))

;; --------------------------- Utils ---------------------------
(defn- create-index [table field]
  (format "CREATE INDEX idx_%s_%s ON %s (%s);" table field table field))

(defn- postgres?->h2
  "If protgres db, leave it as it, otherwise convert to h2."
  [query]
  (if postgres?
    query
    (case (string/upper-case query)
      "TEXT"   "CLOB"
      ;; intentionally have a space here
      "[]"     "ARRAY")))


;; --------------------------- Migrations ---------------------------
(defmigration create-user-table
  ;; we user core_user instead user because user is a preserved table for most dbs
  ;; TODO: can we use TEXT for email, got future not supported when apply not null for CLOB on h2
  (str "CREATE TABLE core_user (
       id SERIAL PRIMARY KEY NOT NULL,
       email VARCHAR(254) NOT NULL UNIQUE,"
       "first_name VARCHAR(254) NOT NULL,
       last_name VARCHAR(254) NOT NULL,
       password VARCHAR(254) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now()
       );"
       (create-index "core_user" "email")))

(defmigration create-page-table
  (str "CREATE TABLE page (
       id SERIAL PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       url VARCHAR(254) NOT NULL,
       tags VARCHAR(32),
       domain VARCHAR(254) NOT NULL,
       path VARCHAR(254) NOT NULL,
       title VARCHAR(254),
       description " (postgres?->h2 "TEXT") ","
       "status VARCHAR(16) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now());"
       (create-index "page" "creator_id")
       (create-index "page" "url")
       (create-index "page" "path")))

(defmigration create-annotation-table
  (str "CREATE TABLE annotation (
       id SERIAL PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       page_id INTEGER NOT NULL REFERENCES page (id) ON DELETE CASCADE,
       color VARCHAR(32) NOT NULL,
       coordinate VARCHAR(254) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now());"
       (create-index "annotation" "creator_id")
       (create-index "annotation" "page_id")
       (create-index "annotation" "coordinate")))

(defmigration create-comment-table
  (str "CREATE TABLE comment (
       id SERIAL PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       annotation_id INTEGER NOT NULL REFERENCES annotation (id) ON DELETE CASCADE,
       content " (postgres?->h2 "TEXT") " NOT NULL,"
       "created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now());"
       (create-index "comment" "creator_id")
       (create-index "comment" "annotation_id")))

(defmigration create-session-table
  (str "CREATE TABLE session (
       id VARCHAR(254) PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       created_at TIMESTAMP NOT NULL DEFAULT now());"
       (create-index "session" "id")))

(defmigration create-password-salt
  (str "ALTER TABLE core_user
       ADD password_salt VARCHAR(254) NOT NULL DEFAULT '';"))

(defmigration include-public-setting-table
  (str "ALTER TABLE page ADD COLUMN public_uuid VARCHAR(254);"
       (create-index "page" "public_uuid")))

(defmigration rename-session-creator-id
  (str "ALTER TABLE session RENAME COLUMN creator_id TO user_id;"))
