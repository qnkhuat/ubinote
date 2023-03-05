(ns ubinote.server.db
  (:require
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [methodical.core :as m]
    [toucan.db :as db]
    [toucan.models :as models]
    [toucan2.core :as tc]
    [ubinote.config :as cfg])
  (:import
    java.util.Properties
    java.io.BufferedReader
    com.mchange.v2.c3p0.ComboPooledDataSource))

;; ------------------------------------------- Extend jdbc protocols -------------------------------------------
(defn clob->str
  "Convert an H2 clob to a String."
  ^String [^org.h2.jdbc.JdbcClob clob]
  (when clob
    (letfn [(->str [^BufferedReader buffered-reader]
              (loop [acc []]
                (if-let [line (.readLine buffered-reader)]
                  (recur (conj acc line))
                  (string/join "\n" acc))))]
      (with-open [reader (.getCharacterStream clob)]
        (if (instance? BufferedReader reader)
          (->str reader)
          (with-open [buffered-reader (BufferedReader. reader)]
            (->str buffered-reader)))))))

;; Proudly stolen from https://github.com/metabase/metabase/blob/master/src/metabase/db/jdbc_protocols.clj
(extend-protocol jdbc/IResultSetReadColumn
  org.postgresql.util.PGobject
  (result-set-read-column [clob _ _]
    (.getValue clob))

  org.h2.jdbc.JdbcClob
  (result-set-read-column [clob _ _]
    (clob->str clob))

  org.h2.jdbc.JdbcBlob
  (result-set-read-column [^org.h2.jdbc.JdbcBlob blob _ _]
    (.getBytes blob 0 (.length blob))))

;; ------------------------------------------- DB connections -------------------------------------------
(defn- connection-pool
  [{:keys [subprotocol subname classname] :as spec}]
  ;; https://github.com/metabase/toucan/blob/29a921750f3051dce350255cfbd33512428bc3f8/docs/connection-pools.md#creating-the-connection-pool
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

(defn- db-details
  [{:keys [db-type db-host db-port db-name]}]
  (connection-pool
   (case db-type
     :h2       {:classname       "org.h2.Driver"
                :subprotocol     "h2:file"
                :subname         (.getAbsolutePath (io/file db-name))
                "MVCC"           "TRUE"
                "DB_CLOSE_DELAY" "-1"
                "DEFRAG_ALWAYS"  "TRUE"}
     :postgres {:classname       "org.postgresql.Driver"
                :subprotocol     "postgresql"
                :subname         (format "//%s:%s/%s"
                                         db-host
                                         db-port
                                         db-name)
                "MVCC"           "TRUE"
                "DB_CLOSE_DELAY" "-1"
                "DEFRAG_ALWAYS"  "TRUE"})))

(def ^:dynamic *application-db*
  (db-details {:db-type (cfg/config-kw :db-type)
               :db-host (cfg/config-str :db-host)
               :db-port (cfg/config-str :db-port)
               :db-name (cfg/config-str :db-name)}))

(m/defmethod tc/do-with-connection :default
  [_connectable f]
  (tc/do-with-connection *application-db* f))

(defn setup-db!
  "TODO: SHOULD BE REMOVED ONCE WE COMPLETELY SWITCH TO TOUCAN 2"
  []
  (let [db-type (cfg/config-kw :db-type)]
    (models/set-root-namespace! 'ubinote.model)
    (db/set-default-quoting-style! (db-type quoting-style))
    (db/set-default-db-connection! (db-details {:db-type (cfg/config-kw :db-type)
                                                :db-host (cfg/config-str :db-host)
                                                :db-port (cfg/config-str :db-port)
                                                :db-name (cfg/config-str :db-name)}))))
