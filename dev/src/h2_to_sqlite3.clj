(ns h2-to-sqlite3
  (:require
   [honey.sql :as sql]
   [honey.sql.helpers :as sql.helpers]
   [next.jdbc :as jdbc]
   [clojure.java.jdbc :as java-jdbc]
   [ubinote.server.db :as db]))

(def h2-ds
  (#'db/connection-pool
   {"DB_CLOSE_DELAY" "-1",
    "DEFRAG_ALWAYS" "TRUE",
    "MVCC" "TRUE",
    :classname "org.h2.Driver",
    :connection-url "jdbc:h2:file:/Users/earther/fun/4_ubinote/ubinote",
    :subprotocol "h2:file"}))

(def sqlite-ds
  (#'db/connection-pool
   {"DB_CLOSE_DELAY" "-1",
    "DEFRAG_ALWAYS" "TRUE",
    "MVCC" "TRUE",
    :classname "org.sqlite.JDBC",
    :connection-url "jdbc:sqlite:file:/Users/earther/fun/4_ubinote/ubinote.sqlite",
    :subprotocol "sqlite"}))


(def tables
  (map name [:core_user
             :page
             :annotation
             :comment]))

(defn- objects->colums+values
  [objs]
  (let [source-keys (keys (first objs))
        quote-fn    (:quote (sql/get-dialect :ansi))
        dest-keys   (for [k source-keys]
                      (quote-fn (name k)))]
    {:cols dest-keys
     :vals (for [row objs]
             (map row source-keys))}))

(defn select-all
  [ds table-name]
  (java-jdbc/query ds [(format "select * from %s" table-name)]))

(map keys (select-all h2-ds "core_user"))

(defn insert-all
  [ds table-name data]
  (let [{:keys [cols vals]} (objects->colums+values data)]
    (java-jdbc/insert-multi! ds table-name cols vals)))

;; DOING IT
#_(doseq [table tables]
    (println "Table" table)
    (insert-all sqlite-ds table (select-all h2-ds table)))

(java-jdbc/query h2-ds [(format "select * from annotation")])
(java-jdbc/query h2-ds [(format "select * from page order by id desc")])


#_(jdbc/execute! h2-ds ["select * from page"])
