(ns archiveio.db
  (:require [clojure.java.io :as io]
            [toucan.db :as db])
  (:import java.util.Properties
           com.mchange.v2.c3p0.ComboPooledDataSource))

(defn connection-pool
  [ {:keys [subprotocol subname classname] :as spec}]
  ; https://github.com/metabase/toucan/blob/29a921750f3051dce350255cfbd33512428bc3f8/docs/connection-pools.md#creating-the-connection-pool
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass                  classname)
                 (.setJdbcUrl                      (str "jdbc:" subprotocol ":" subname))
                 (.setMaxIdleTimeExcessConnections (* 30 60))   ; 30 seconds
                 (.setMaxIdleTime                  (* 3 60 60)) ; 3 minutes
                 (.setInitialPoolSize              3)
                 (.setMinPoolSize                  3)
                 (.setMaxPoolSize                  15)
                 (.setIdleConnectionTestPeriod     0)
                 (.setTestConnectionOnCheckin      false)
                 (.setTestConnectionOnCheckout     false)
                 (.setPreferredTestQuery           nil)
                 ;; set all other values of the DB spec besides subprotocol, subname, and classname as properties of the connection pool
                 (.setProperties                   (let [properties (Properties.)]
                                                     (doseq [[k v] (dissoc spec :classname :subprotocol :subname)]
                                                       (.setProperty properties (name k) (str v)))
                                                     properties)))})

(defn setup-db!
  [db]
  (db/set-default-db-connection!
    (connection-pool {:classname       "org.h2.Driver"
                      :subprotocol     "h2:file"
                      :subname         (.getAbsolutePath (io/file db))
                      "MVCC"           "TRUE"
                      "DB_CLOSE_DELAY" "-1"
                      "DEFRAG_ALWAYS"  "TRUE"})))
