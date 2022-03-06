(ns archiveio.api.annotation
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.api.response :as resp]
            [archiveio.controller.annotation :as ant]
            [archiveio.model.annotation :refer [Annotation]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def ^:private validate-create-annotation
  "Schema for adding a user"
  (s/validator ant/NewAnnotation))

(defn create-annotation
  [{:keys [params] :as _req}]
  ;; TODO :user-id should take from req
  (validate-create-annotation params)
  (resp/entity-response 200 (ant/create params)))

(defn get-annotation
  [id _req]
  (let [annotation (-> (db/select-one Annotation :id id)
                       (hydrate :user))]
    (resp/assert-404 annotation "Annotation not found")
    (resp/entity-response 200 annotation)))

(defroutes routes
  (POST "/" [] create-annotation)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-annotation id))))

