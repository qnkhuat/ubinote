(ns ubinote.api.annotation
  (:require
    [compojure.coercions :refer [as-int]]
    [compojure.core :refer [context defroutes POST DELETE]]
    [schema.core :as s]
    [toucan2.core :as tc]
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
       (tc/insert! Annotation)))

(defn- delete-annotation
  [id _req]
  (api/check-404 (tc/select-one Annotation :id id))
  (tc/delete! Annotation :id id))

(defroutes routes
  (POST "/" [] create)
  (context "/:id" [id :<< as-int]
           (DELETE "/" [] (partial delete-annotation id))))
