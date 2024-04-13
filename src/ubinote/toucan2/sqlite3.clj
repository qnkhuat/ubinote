(ns ubinote.toucan2.sqlite3
  "Some hacks so toucan2 works with sqlite3 properly until toucan2#169 is fixed."
  (:require
   [honey.sql.helpers :as sql.helpers]
   [methodical.core :as m]
   [toucan2.core :as tc]
   [toucan2.jdbc.query :as tc.jdbc.query]
   [toucan2.pipeline :as tc.pipeline]
   [ubinote.server.db :as db]))

(defn apply-if-sqlite
  "Apply thunk to result if the current DB is SQLite."
  [result thunk]
  (if (= :sqlite (db/db-type))
    (thunk result)
    result))

(m/defmethod tc.pipeline/build :around [#_query-type :toucan.query-type/insert.pks
                                        #_model      :default
                                        #_query      :default]
  "Build a Honey SQL 2 SELECT query."
  [query-type model parsed-args resolved-query]
  (apply-if-sqlite (next-method query-type model parsed-args resolved-query)
                   #(apply sql.helpers/returning % (tc/primary-keys model))))

(m/defmethod tc.pipeline/build :around [#_query-type :toucan.query-type/insert.instances
                                        #_model      :default
                                        #_query      :default]
  "Build a Honey SQL 2 SELECT query."
  [query-type model parsed-args resolved-query]
  (apply-if-sqlite (next-method query-type model parsed-args resolved-query)
                   #(apply sql.helpers/returning % (tc/primary-keys model))))

(m/defmethod tc.pipeline/build :around [#_query-type :toucan.query-type/update.pks
                                        #_model      :default
                                        #_query      :default]
  "Build a Honey SQL 2 SELECT query."
  [query-type model parsed-args resolved-query]
  (apply-if-sqlite (next-method query-type model parsed-args resolved-query)
                   #(apply sql.helpers/returning % (tc/primary-keys model))))

;; Override the implementation in toucan2.jdbc.pipeline
(m/defmethod tc.pipeline/transduce-execute-with-connection [#_connection java.sql.Connection
                                                            #_query-type :default
                                                            #_model      :default]
  "Default impl for the JDBC query execution backend."
  [rf conn query-type model sql-args]
  {:pre [(sequential? sql-args) (string? (first sql-args))]}
  ;; `:return-keys` is passed in this way instead of binding a dynamic var because we don't want any additional queries
  ;; happening inside of the `rf` to return keys or whatever.
  (let [extra-options (when (isa? query-type :toucan.result-type/pks)
                        ;; only change is here, sqlite relied on RETURNING clause, so we don't need the returned keys
                        {:return-keys (not= (db/db-type) :sqlite)})
        result        (tc.jdbc.query/reduce-jdbc-query rf (rf) conn model sql-args extra-options)]
    (rf result)))
