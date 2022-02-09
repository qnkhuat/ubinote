(ns archiveio.migration
  (:require [archiveio.model.migration :refer [Migration]]
            [clojure.java.jdbc :as jdbc]
            [taoensso.timbre :as log]
            [toucan.db :as db]))

(def migrations (atom []))

(defn- create-migrations-table-if-needed! []
  (jdbc/execute! (db/connection) ["CREATE TABLE IF NOT EXISTS migration (name VARCHAR PRIMARY KEY NOT NULL);"]))

(defn- previous-migrations []
  (db/select-field :name Migration))

(defn- migrate!*
  "Runs DB migrations. Copied from metastore."
  []
  (log/info "Running migrations if needed...")
  (create-migrations-table-if-needed!)
  (let [the-previous-migrations (previous-migrations)]
    (db/transaction
      (doseq [[migration-name statements] @migrations]
        (when-not (the-previous-migrations migration-name)
          (doseq [statement statements]
            (log/info "Running migration " migration-name)
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
