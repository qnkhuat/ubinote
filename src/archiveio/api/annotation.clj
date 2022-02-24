(ns archiveio.api.annotation
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.api.response :as resp]
            [archiveio.controller.annotation :as ant]
            [archiveio.model.annotation :refer [Annotation]]
            [schema.core :as s]
            [toucan.db :as db]))

(def ^:private validate-create-annotation
  "Schema for adding a user"
  (s/validator ant/NewAnnotation))

(defn create-annotation
  [{:keys [params] :as _req}]
  ;; TODO :user-id shoudl take from req
  (validate-create-annotation params)
  (resp/entity-response 200 (ant/create params)))

(defn get-annotation
  [id _req]
  (let [annotation (db/select-one Annotation :id id)]
    (resp/assert-404 annotation "Annotation not found")
    (resp/entity-response 200 annotation)))

(defn list-annotations
  [_req]
  (resp/entity-response 200 (db/select Annotation)))

(defroutes routes
  (GET "/" [] list-annotations)
  (POST "/" [] create-annotation)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-annotation id))))

