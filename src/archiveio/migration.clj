(ns archiveio.migration
  "This migration should works for both h2 and postgres"
  (:require [archiveio.model.migration :refer [Migration]]
            [archiveio.config :as cfg]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [toucan.db :as db]))
(def migrations (atom #{}))

(def postgres? (= :postgres (cfg/config-kw :aio-db-type)))

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
          (log/info "Running migration " migration-name)
          (doseq [statement statements]
            (try
              (jdbc/execute! (db/connection) statement)
              (catch Exception e
                (log/error (format "Data migration %s failed: %s" migration-name (.getMessage e))))))
          (db/insert! Migration :name migration-name)))))
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

(when postgres?
  (defmigration install-citext
    "CREATE EXTENSION IF NOT EXISTS citext;"))

(defn- postgres?->h2
  "If protgres db, leave it as it, otherwise convert to h2"
  [query]
  (if postgres?
    query
    (case (string/upper-case query)
      "TEXT"  "CLOB"
      "CITEXT" "VARCHAR_IGNORECASE(255)")))

(defmigration create-user-table
  (str "CREATE TABLE \"user\" (
       id SERIAL PRIMARY KEY NOT NULL,
       email " (postgres?->h2 "CITEXT") " NOT NULL UNIQUE,"
       "first_name VARCHAR(255) NOT NULL,
       last_name VARCHAR(255) NOT NULL,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now()
       );"
       "CREATE INDEX idx_user_email ON \"user\" (email);"))

;; TODO add status
(defmigration create-archive-table
  (str "CREATE TABLE archive (
       id SERIAL PRIMARY KEY NOT NULL,
       url VARCHAR(255) NOT NULL,
       path VARCHAR(255) NOT NULL,
       status VARCHAR(16) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       updated_at TIMESTAMP NOT NULL DEFAULT now());"))
