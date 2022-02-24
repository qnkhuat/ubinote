(ns archiveio.db
  (:require [archiveio.config :as cfg]
            [clojure.java.io :as io]
            [toucan.db :as db])
  (:import java.util.Properties
           com.mchange.v2.c3p0.ComboPooledDataSource))

(defn connection-pool
  [{:keys [subprotocol subname classname] :as spec}]
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

(def quoting-style
  {:postgres :ansi
   :h2       :h2
   :mysql    :mysql})

(defn db-details
  [db-type]
  (connection-pool
    (case db-type
      :h2       {:classname       "org.h2.Driver"
                 :subprotocol     "h2:file"
                 :subname         (.getAbsolutePath (io/file (cfg/config-str :aio-db-name)))
                 "MVCC"           "TRUE"
                 "DB_CLOSE_DELAY" "-1"
                 "DEFRAG_ALWAYS"  "TRUE"}
      :postgres {:classname       "org.postgresql.Driver"
                 :subprotocol     "postgresql"
                 :subname         (format "//%s:%s/%s"
                                          (cfg/config-str :aio-db-host)
                                          (cfg/config-str :aio-port)
                                          (cfg/config-str :aio-db-name))
                 "MVCC"           "TRUE"
                 "DB_CLOSE_DELAY" "-1"
                 "DEFRAG_ALWAYS"  "TRUE"
                 })))

(defn setup-db!
  []
  (let [db-type (cfg/config-kw :aio-db-type)]
    (db/set-default-automatically-convert-dashes-and-underscores! true)
    (db/set-default-quoting-style! (db-type quoting-style))
    (db/set-default-db-connection!
      (db-details db-type))))
