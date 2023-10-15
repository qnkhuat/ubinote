(ns ubinote.api.annotation
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes PUT POST DELETE]]
   [malli.core :as mc]
   [medley.core :as m]
   [toucan2.core :as tc]
   [ubinote.api.common :as api]
   [ubinote.util :as u]))

(def NewAnnotation
  (mc/schema
   [:map {:closed true}
    [:page_id                :int]
    [:creator_id             :int]
    [:coordinate             [:map {:closed true}
                              [:start number?]
                              [:end   number?]]]
    [:color {:optional true} [:maybe :string]]]))

(defn- create
  [{:keys [body] :as _req}]
  (->> #p (assoc body :creator_id api/*current-user-id*)
       (api/validate NewAnnotation)
       (tc/insert-returning-instances! :m/annotation)
       first))

(def UpdateAnnotation
  (mc/schema
   [:map
    [:comments [:sequential :string]]]))

(defn- update-annotation
  [id {:keys [body] :as _req}]
  (let [payload (select-keys body [:color :coordinate])]
    (when (seq payload)
      (tc/update! :m/annotation id payload)))
  ;; Update annotation lomments if needed
  (when-let [comments (seq (:comments body))]
    (let [current-comments                        (tc/select [:m/comment :id :content] :annotation_id id)
          id->comment                             (m/index-by :id current-comments)
          {:keys [to-create to-delete to-update]} (u/classify-changes current-comments
                                                                      (map #(select-keys % [:id :content]) comments))]
      (when (seq to-create)
        (tc/insert! :m/comment (->> to-create
                                    (map #(assoc %
                                                 :annotation_id id
                                                 :creator_id api/*current-user-id*))
                                    (map #(dissoc % :id)))))
      ;; TODO: this does update every time
      (when (seq to-update)
        (doseq [update-item to-update]
          (when-not (= (:content update-item) (:content (get id->comment (:id update-item))))
            (tc/update! :m/comment (:id update-item) {:content (:content update-item)}))))
      (when (seq to-delete)
        (tc/delete! :m/comment :id [:in (map :id to-delete)]))))
  (tc/hydrate (tc/select-one :m/annotation id) :comments))

(defn- delete-annotation
  [id _req]
  (api/check-404 (tc/select-one :m/annotation :id id))
  (tc/delete! :m/annotation :id id)
  api/generic-204-response)

(defroutes routes
  (POST "/" [] create)
  (context "/:id" [id :<< as-int]
           (PUT "/" [] (partial update-annotation id))
           (DELETE "/" [] (partial delete-annotation id))))
