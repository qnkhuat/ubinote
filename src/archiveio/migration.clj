(ns archiveio.migration
  (:require [archiveio.model.migration :refer [Migration]]
            [clojure.java.jdbc :as jdbc]
            [taoensso.timbre :as log]
            [toucan.db :as db]))

(def migrations (atom #{}))

(conj #{} [1 2])

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
  (swap! migrations conj [migration-name sql-statements])
  nil)

(defmacro ^:private defmigration {:style/indent 1} [migration-name & sql-statements]
  `(defmigration* ~(str migration-name) ~@sql-statements))


(defmigration create-user-table
  "CREATE TABLE \"user\" (
  id SERIAL PRIMARY KEY NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
  );"
  "CREATE INDEX idx_user_email ON \"user\" (email);"
  )
