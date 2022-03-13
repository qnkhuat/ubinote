(ns ubinote.api.comment
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.model.comment :refer [Comment]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def NewComment
  {:annotation-id s/Int
   :content       s/Str})

(def ^:private validate-create-comment
  "Schema for adding a user"
  (s/validator NewComment))

(defn create-comment
  [{:keys [params current-user] :as _req}]
  (validate-create-comment params)
  (db/insert! Comment (assoc params :user-id (:id current-user))))

(defn get-comment
  [id _req]
  (let [comment (-> (db/select-one Comment :id id)
                    (hydrate :annotation :user))]
    (api/check-404 comment)
    comment))

(defroutes routes
  (POST "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-comment id))))

