(ns archiveio.api.comment
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.api.response :as resp]
            [archiveio.controller.comment :as cmt]
            [archiveio.model.comment :refer [Comment]]
            [schema.core :as s]
            [toucan.db :as db]))

(def ^:private validate-create-comment
  "Schema for adding a user"
  (s/validator cmt/NewComment))

;; TODO add convert clob for h2

(defn create-comment
  [{:keys [params] :as _req}]
  ;; TODO :user-id shoudl take from req
  (validate-create-comment params)
  (resp/entity-response 200 (dissoc (cmt/create params) :content)))

(defn get-comment
  [id _req]
  (let [comment (db/select-one Comment :id id)]
    (resp/assert-404 comment  "Comment not found")
    (resp/entity-response 200 (dissoc comment :content))))

(defroutes routes
  (POST "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-comment id))))

