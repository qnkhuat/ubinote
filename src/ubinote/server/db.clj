(ns ubinote.server.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as string]
   [honey.sql :as sql]
   [methodical.core :as m]
   [toucan2.core :as tc]
   [toucan2.honeysql2 :as t2.honeysql]
   [toucan2.pipeline :as tc.pipeline]
   [ubinote.config :as cfg])
  (:import
   java.util.Properties
   java.io.BufferedReader
   com.mchange.v2.c3p0.ComboPooledDataSource))

(def supported-dbms
  "List of dbms we support."
  #{:postgres :h2 :sqlite})

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

;; Proudly stolen from https://github.com/metabase/metabase/blob/master/src/metabase/jdbc_protocols.clj
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

(def ^:private db-type->dialect-name
  {:postgresql :ansi
   :h2         :h2
   :sqlite     :ansi})

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
      :h2         {:classname   "org.h2.Driver"
                   :subprotocol "h2:file"}
      :postgresql {:classname   "org.postgresql.Driver"
                   :subprotocol "postgresql"}))))

(defn- english-upper-case
  "Use this function when you need to upper-case an identifier or table name. Similar to `clojure.string/upper-case`
  but always converts the string to upper-case characters in the English locale. Using `clojure.string/upper-case` for
  table names, like we are using below in the `:h2` `honeysql.format` function can cause issues when the user has
  changed the locale to a language that has different upper-case characters. Turkish is one example, where `i` gets
  converted to `İ`. This causes the `SETTING` table to become the `SETTİNG` table, which doesn't exist."
  [^CharSequence s]
  (-> s str (.toUpperCase java.util.Locale/ENGLISH)))

(sql/register-dialect!
 :h2
 (update (sql/get-dialect :ansi) :quote (fn [quote]
                                          (comp english-upper-case quote))))

(def ^:private application-db
  (db-details (cfg/config-str :db-connection-url)))

(m/defmethod tc.pipeline/build :around :default
  "Normally, our Honey SQL 2 `:dialect` is set to `::application-db`; however, Toucan 2 does need to know the actual
  dialect to do special query building magic. When building a Honey SQL form, make sure `:dialect` is bound to the
  *actual* dialect for the application database."
  [query-type model parsed-args resolved-query]
  (binding [t2.honeysql/*options* (assoc t2.honeysql/*options*
                                         :dialect (db-type->dialect-name (db-type)))]
    (next-method query-type model parsed-args resolved-query)))


(m/defmethod tc/do-with-connection :default
  [_connectable f]
  (tc/do-with-connection application-db f))

(reset! t2.honeysql/global-options
        {:quoted       true
         :dialect      (-> (db-type) db-type->dialect-name sql/get-dialect :dialect)
         :quoted-snake false})
