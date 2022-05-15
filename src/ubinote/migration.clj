(ns ubinote.migration
  "This migration should works for both h2 and postgres"
  (:require [ubinote.models.migration :refer [Migration]]
            [ubinote.config :as cfg]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [toucan.db :as db]))

(def migrations (atom []))

(def postgres? (= :postgres (cfg/config-kw :un-db-type)))

(defn- create-migrations-table-if-needed! []
  (jdbc/execute! (db/connection) ["CREATE TABLE IF NOT EXISTS migration (name VARCHAR PRIMARY KEY NOT NULL);"]))

(defn- previous-migrations []
  (set (db/select-field :name Migration)))

(defn- migrate!*
  "Runs DB migrations. Copied from metastore."
  []
  (log/info "Running migrations if needed...")
  (create-migrations-table-if-needed!)
  (when-let [the-previous-migrations (previous-migrations)]
    (db/transaction
      (doseq [[migration-name statements] @migrations]
        (when-not (the-previous-migrations migration-name)
          (log/info "Running migration" migration-name)
          (try
            (doseq [statement statements]
              (jdbc/execute! (db/connection) statement))
            (db/insert! Migration :name migration-name)
            (catch Exception e
              (throw (ex-info (format "Data migration %s failed: \n%s\n%s" migration-name (string/join "\n" statements) (.getMessage e)) {}))))))))
  (log/info "Migrations finished"))


(def ^{:doc "Memoized migrate!* to only run once."} migrate!
  (memoize migrate!*))

(defn- defmigration* [migration-name & sql-statements]
  (if (some #(= (str migration-name) (first %)) @migrations)
    (throw (ex-info (format "Migration with name %s existed" migration-name) {:migration-name migration-name}))
    (swap! migrations conj [migration-name sql-statements]))
  nil)

(defmacro ^:private defmigration {:style/indent 1} [migration-name & sql-statements]
  `(defmigration* ~(str migration-name) ~@sql-statements))

;; --------------------------- Utils ---------------------------
(defn- create-index [table field]
  (format "CREATE INDEX idx_%s_%s ON %s (%s);" table field table field))

(when postgres?
  (defmigration install-citext
    "CREATE EXTENSION IF NOT EXISTS citext;"))

(when postgres?
  (defmigration install-uuid-ossp
    "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"))

(defn- postgres?->h2
  "If protgres db, leave it as it, otherwise convert to h2"
  [query]
  (if postgres?
    query
    (case (string/upper-case query)
      "TEXT"   "CLOB"
      "CITEXT" "VARCHAR_IGNORECASE(255)"
      "UUID_GENERATE_V4()" "RANDOM_UUID()"
      ;; intentionally have a space here
      "[]"     " ARRAY")))


;; --------------------------- Migrations ---------------------------
(defmigration create-user-table
  ;; we user core_user instead user because user is a preserved table for most dbs
  (str "CREATE TABLE core_user (
       id SERIAL PRIMARY KEY NOT NULL,
       email " (postgres?->h2 "CITEXT") " NOT NULL UNIQUE,"
       "first_name VARCHAR(255) NOT NULL,
       last_name VARCHAR(255) NOT NULL,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now()
       );"
       (create-index "core_user" "email")))

(defmigration create-page-table
  (str "CREATE TABLE page (
       id SERIAL PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       url VARCHAR(255) NOT NULL,
       tags VARCHAR(32)" (postgres?->h2 "[]") ","
       "domain VARCHAR(255) NOT NULL,
       path VARCHAR(255) NOT NULL,
       title VARCHAR(255),
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
       coordinate VARCHAR(255) NOT NULL,
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
       id UUID DEFAULT "(postgres?->h2 "uuid_generate_v4()")" PRIMARY KEY NOT NULL,
       creator_id INTEGER NOT NULL REFERENCES core_user (id) ON DELETE CASCADE,
       created_at TIMESTAMP NOT NULL DEFAULT now());"
       (create-index "session" "id")))
