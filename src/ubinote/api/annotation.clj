(ns ubinote.api.annotation
  (:require
    [compojure.coercions :refer [as-int]]
    [compojure.core :refer [context defroutes POST GET DELETE]]
    [schema.core :as s]
    [toucan.db :as db]
    [toucan.hydrate :refer [hydrate]]
    [ubinote.api.common :as api]
    [ubinote.models.annotation :refer [Annotation]]))

(def NewAnnotation
  {:page_id                s/Int
   :creator_id             s/Int
   :coordinate             {:start s/Num
                            :end   s/Num}
   (s/optional-key :color) (s/maybe s/Str)})

(defn- create
  [{:keys [body] :as _req}]
  (->> (assoc body :creator_id api/*current-user-id*)
       (db/insert! Annotation)))

(defn- get-annotation
  [id _req]
  (-> (db/select-one Annotation :id id)
      (hydrate :user)
      api/check-404))

(defn- delete-annotation
  [id _req]
  (api/check-404 (db/select-one Annotation :id id))
  (db/delete! Annotation :id id))

(defroutes routes
  (POST "/" [] create)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-annotation id))
           (DELETE "/" [] (partial delete-annotation id))))
