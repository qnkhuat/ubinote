(ns archiveio.db
  (:require [archiveio.config :as cfg]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [toucan.models :as models]
            [toucan.db :as db])
  (:import java.util.Properties
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

(defn- db-details
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
                                          (cfg/config-str :aio-db-port)
                                          (cfg/config-str :aio-db-name))
                 "MVCC"           "TRUE"
                 "DB_CLOSE_DELAY" "-1"
                 "DEFRAG_ALWAYS"  "TRUE"})))

(db/set-default-db-connection!
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     "//localhost:5432/archiveio"
   ;:user        "cam"
   })

(db/select-one archiveio.model.annotation/Annotation)

(defn setup-db!
  []
  (let [db-type (cfg/config-kw :aio-db-type)]
    (models/set-root-namespace! 'archiveio.model)
    (db/set-default-automatically-convert-dashes-and-underscores! true)
    (db/set-default-quoting-style! (db-type quoting-style))
    (db/set-default-db-connection!
      (db-details db-type))))
