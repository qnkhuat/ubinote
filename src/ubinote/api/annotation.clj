(ns ubinote.api.annotation
  (:require
   [cheshire.core :as json]
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes POST DELETE]]
   [malli.core :as mc]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.ui :as ui]))

(def NewAnnotation
  (mc/schema
   [:map {:closed true}
    [:page_id                :int]
    [:creator_id             :int]
    [:coordinate             [:map {:closed true}
                              [:start number?]
                              [:end   number?]]]
    [:color {:optional true} [:maybe :string]]]))

(defn- create-annotation
  [{:keys [params] :as _req}]
  (->> (-> params
           (assoc :creator_id api.u/*current-user-id*)
           (update :page_id parse-long)
           (update :coordinate #(json/parse-string % keyword)))
       (api.u/decode NewAnnotation)
       (tc/insert-returning-instance! :m/annotation)
       (ui/render :annotation)
       ui/render-hiccup-fragment))

(defn- delete-annotation
  [id _req]
  (api.u/check-404 (tc/select-one :m/annotation :id id))
  (tc/delete! :m/annotation :id id)
  api.u/generic-200-response)

(defroutes routes
  (POST "/" [] create-annotation)
  (context "/:id" [id :<< as-int]
           (DELETE "/" [] (partial delete-annotation id))))
