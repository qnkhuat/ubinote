(ns ubinote.migration
  "This migration should works for both sqlite and postgres"
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [next.jdbc :as jdbc]
   [toucan2.core :as tc]
   [ubinote.server.db :as db]))

(def migrations (atom []))

(defn- create-index [table field]
  (format "CREATE INDEX idx_%s_%s ON %s (%s);" table field table field))

(defn- postgres?->sqlite
  "If protgres db, leave it as it, otherwise convert to sqlite"
  [query]
  (case (db/db-type)
    :postgres
    query

    :sqlite
    (case (str/upper-case query)
      "NOW()" "CURRENT_TIMESTAMP"
      "SERIAL" "INTEGER" ;; integer primary key
      query)))

(defn- create-migrations-table-if-needed! []
  (log/info "Creating migration table if needed...")
  (tc/with-connection [conn]
    (jdbc/execute! conn [(format "CREATE TABLE IF NOT EXISTS migration (
                                 name VARCHAR PRIMARY KEY NOT NULL,
                                 created_at TIMESTAMP NOT NULL DEFAULT %s);"
                                 (postgres?->sqlite "now()"))])))

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
           (log/infof "Migration statement:\n%s" statement-or-map)
           (jdbc/execute! conn [statement-or-map])
           (tc/insert! :m/migration :name migration-name)
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

;; --------------------------- Migrations ---------------------------
(defmigration create-user-table
  ;; we user core_user instead user because user is a preserved table for most dbs
  ;; TODO: can we use TEXT for email, got future not supported when apply not null
  (str "CREATE TABLE core_user (
       id "(postgres?->sqlite "SERIAL")" PRIMARY KEY NOT NULL,
       email VARCHAR(254) NOT NULL UNIQUE,
       first_name VARCHAR(254) NOT NULL,
       last_name VARCHAR(254) NOT NULL,
       password VARCHAR(254) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")",
       updated_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")");"
       (create-index "core_user" "email")))

(defmigration create-page-table
  (str "CREATE TABLE page (
       id "(postgres?->sqlite "SERIAL")" PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       url VARCHAR(254) NOT NULL,
       tags VARCHAR(32),
       domain VARCHAR(254) NOT NULL,
       path VARCHAR(254) NOT NULL,
       title VARCHAR(254),
       description " (postgres?->sqlite "TEXT") ","
       "status VARCHAR(16) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")",
       updated_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")");"
       (create-index "page" "creator_id")
       (create-index "page" "url")
       (create-index "page" "path")))

(defmigration create-annotation-table
  (str "CREATE TABLE annotation (
       id "(postgres?->sqlite "SERIAL")" PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       page_id INTEGER NOT NULL REFERENCES page (id) ON DELETE CASCADE,
       color VARCHAR(32) NOT NULL,
       coordinate VARCHAR(254) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")",
       updated_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")");"
       (create-index "annotation" "creator_id")
       (create-index "annotation" "page_id")
       (create-index "annotation" "coordinate")))

(defmigration create-comment-table
  (str "CREATE TABLE comment (
       id "(postgres?->sqlite "SERIAL")" PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       annotation_id INTEGER NOT NULL REFERENCES annotation (id) ON DELETE CASCADE,
       content " (postgres?->sqlite "TEXT") " NOT NULL,"
       "created_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")",
       updated_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")");"
       (create-index "comment" "creator_id")
       (create-index "comment" "annotation_id")))

(defmigration create-session-table
  (str "CREATE TABLE session (
       id VARCHAR(254) PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       created_at TIMESTAMP NOT NULL DEFAULT "(postgres?->sqlite "now()")");"
       (create-index "session" "id")))

(defmigration create-password-salt
  (str "ALTER TABLE core_user
       ADD password_salt VARCHAR(254) NOT NULL DEFAULT '';"))

(defmigration include-public-setting-table
  (str "ALTER TABLE page ADD COLUMN public_uuid VARCHAR(254);"
       (create-index "page" "public_uuid")))

(defmigration rename-session-creator-id
  (str "ALTER TABLE session RENAME COLUMN creator_id TO user_id;"))
