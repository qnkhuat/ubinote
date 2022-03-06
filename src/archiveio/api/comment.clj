(ns archiveio.api.comment
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.api.common :as api]
            [archiveio.controller.comment :as cmt]
            [archiveio.model.comment :refer [Comment]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def ^:private validate-create-comment
  "Schema for adding a user"
  (s/validator cmt/NewComment))

(defn create-comment
  [{:keys [params] :as _req}]
  ;; TODO :user-id shoudl take from req
  (validate-create-comment params)
  (cmt/create params))

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

