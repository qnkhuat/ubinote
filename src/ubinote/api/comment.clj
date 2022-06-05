(ns ubinote.api.comment
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models.comment :refer [Comment]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def NewComment
  {:annotation_id s/Int
   :creator_id    s/Int
   :content       s/Str})

(def ^:private validate-create-comment
  "Schema for adding a user"
  (s/validator NewComment))

(defn create-comment
  [{:keys [params current-user] :as _req}]
  (let [cmt (assoc params :creator_id (:id current-user))]
   (validate-create-comment cmt)
   (db/insert! Comment cmt)))

(defn get-comment
  [id _req]
  (-> (db/select-one Comment :id id)
      (hydrate :annotation :user)
      api/check-404))

(defroutes routes
  (POST "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-comment id))))
