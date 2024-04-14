(ns ubinote.toucan2
  (:require
   [honey.sql :as sql]
   [methodical.core :as m]
   [toucan2.core :as tc]
   [toucan2.honeysql2 :as tc.honeysql]
   [toucan2.pipeline :as tc.pipeline]
   [ubinote.server.db :as db]
   [ubinote.toucan2.sqlite3]))


(comment
 ;; need this so toucan2 works properly with sqlite
 ubinote.toucan2.sqlite3)

;; ------------------------------------------- Toucan2 setup -------------------------------------------

(def ^:private db-type->dialect-name
  {:postgresql :ansi
   :sqlite     :ansi})

(m/defmethod tc.pipeline/build :around :default
  [query-type model parsed-args resolved-query]
  (binding [tc.honeysql/*options* (assoc tc.honeysql/*options*
                                         :dialect (db-type->dialect-name (db/db-type)))]
    (next-method query-type model parsed-args resolved-query)))

(m/defmethod tc/do-with-connection :default
  [_connectable f]
  (tc/do-with-connection db/*application-db* f))

(reset! tc.honeysql/global-options
        {:quoted       true
         :dialect      (-> (db/db-type) db-type->dialect-name sql/get-dialect :dialect)
         :quoted-snake false})
