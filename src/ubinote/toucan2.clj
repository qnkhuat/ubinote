(ns ubinote.toucan2
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as string]
   [honey.sql :as sql]
   [methodical.core :as m]
   [toucan2.core :as tc]
   [toucan2.honeysql2 :as tc.honeysql]
   [toucan2.pipeline :as tc.pipeline]
   [ubinote.server.db :as db])
  (:import
   java.io.BufferedReader))

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

;; ------------------------------------------- Toucan2 setup -------------------------------------------

(defn- english-upper-case
  "Use this function when you need to upper-case an identifier or table name. Similar to `clojure.string/upper-case`
  but always converts the string to upper-case characters in the English locale. Using `clojure.string/upper-case` for
  table names, like we are using below in the `:h2` `honeysql.format` function can cause issues when the user has
  changed the locale to a language that has different upper-case characters. Turkish is one example, where `i` gets
  converted to `İ`. This causes the `SETTING` table to become the `SETTİNG` table, which doesn't exist."
  [^CharSequence s]
  (-> s str (.toUpperCase java.util.Locale/ENGLISH)))

(def ^:private db-type->dialect-name
  {:postgresql :ansi
   :h2         :h2
   :sqlite     :ansi})

(sql/register-dialect!
 :h2
 (update (sql/get-dialect :ansi) :quote (fn [quote]
                                          (comp english-upper-case quote))))

(m/defmethod tc.pipeline/build :around :default
  [query-type model parsed-args resolved-query]
  (binding [tc.honeysql/*options* (assoc tc.honeysql/*options*
                                         :dialect (db-type->dialect-name (db/db-type)))]
    (next-method query-type model parsed-args resolved-query)))

(m/defmethod tc/do-with-connection :default
  [_connectable f]
  (tc/do-with-connection db/application-db f))

(reset! tc.honeysql/global-options
        {:quoted       true
         :dialect      (-> (db/db-type) db-type->dialect-name sql/get-dialect :dialect)
         :quoted-snake false})
