(ns ubinote.server.db
  (:require
   [ubinote.config :as cfg])
  (:import
   java.util.Properties
   com.mchange.v2.c3p0.ComboPooledDataSource))

(def supported-dbms
  "List of dbms we support."
  #{:postgres :sqlite})

;; ------------------------------------------- DB connections -------------------------------------------
(defn- connection-pool
  [{:keys [connection-url classname] :as spec}]
  ;; https://github.com/metabase/toucan/blob/29a921750f3051dce350255cfbd33512428bc3f8/docs/connection-pools.md#creating-the-connection-pool
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass                  classname)
                 (.setJdbcUrl                      connection-url #_(str "jdbc:" subprotocol ":" subname))
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

(defn db-type
  "Return the db type based on connection-url"
  ([]
   (db-type (cfg/config-str :db-connection-url)))
  ([connection-url]
   (keyword (second (re-find #"jdbc:([^:]+):" connection-url)))))

(defn- db-details
  [connection-url]
  (connection-pool
   (merge
    {"MVCC"           "TRUE"
     "DB_CLOSE_DELAY" "-1"
     "DEFRAG_ALWAYS"  "TRUE"
     :connection-url  connection-url}
    (case (db-type connection-url)
      :sqlite     {:classname   "org.sqlite.JDBC"
                   :subprotocol "sqlite"}
      :postgresql {:classname   "org.postgresql.Driver"
                   :subprotocol "postgresql"}))))

(def application-db
  "The application db details."
  (db-details (cfg/config-str :db-connection-url)))
