(ns ubinote.api.annotation
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.model.annotation :refer [Annotation]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def NewAnnotation
  {:user-id    s/Int
   :page-id    s/Int
   :color      s/Str
   :coordinate s/Str
   })

(s/defn create
  "Detect file type and page file"
  [annotation :- NewAnnotation]
  (db/insert! Annotation annotation))

(def ^:private validate-create-annotation
  "Schema for adding a user"
  (s/validator NewAnnotation))

(defn create-annotation
  [{:keys [params] :as _req}]
  ;; TODO :user-id should take from req
  (validate-create-annotation params)
  (db/insert! Annotation params))

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

