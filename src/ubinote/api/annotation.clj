(ns ubinote.api.annotation
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.controller.annotation :as ant]
            [ubinote.model.annotation :refer [Annotation]]
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
  (ant/create params))

(defn get-annotation
  [id _req]
  (let [annotation (-> (db/select-one Annotation :id id)
                       (hydrate :user))]
    (api/check-404 annotation)
    annotation))

(defroutes routes
  (POST "/" [] create-annotation)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-annotation id))))

