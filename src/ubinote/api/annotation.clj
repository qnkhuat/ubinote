(ns ubinote.api.annotation
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models.annotation :refer [Annotation]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def NewAnnotation
  {:page_id                s/Int
   :creator_id             s/Int
   :coordinate             {:start s/Num
                            :end   s/Num}
   (s/optional-key :color) (s/maybe s/Str)})

(s/defn create
  "Detect file type and page file"
  [annotation :- NewAnnotation]
  (db/insert! Annotation annotation))

(def ^:private validate-create-annotation
  "Schema for adding a user"
  (s/validator NewAnnotation))

(defn create
  [{:keys [body] :as _req}]
  (->> (assoc body :creator_id api/*current-user-id*)
       validate-create-annotation
       (db/insert! Annotation)))

(defn get-annotation
  [id _req]
  (-> (db/select-one Annotation :id id)
      (hydrate :user)
      api/check-404))

(defroutes routes
  (POST "/" [] create)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-annotation id))))
