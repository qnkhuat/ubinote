(ns ubinote.api.annotation
  (:require
   [cheshire.core :as json]
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes POST DELETE]]
   [java-time :as t]
   [malli.core :as mc]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.ui.core :as ui]
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

(defn- create-annotation
  [{:keys [params] :as _req}]
  (->> (-> params
           (assoc :creator_id api.u/*current-user-id*)
           (update :page_id parse-long)
           (update :coordinate #(json/parse-string % keyword)))
       (api.u/validate NewAnnotation)
       (tc/insert-returning-instance! :m/annotation)
       (ui/render :annotation)
       ui/hiccup->html-response))

(def NewComment
  [:map
   [:annotation_id :int]
   [:creator_id    :int]
   [:content       :string]])

(defmethod ui/render :comment
  [_component {:keys [id content creator_email created_at] :as _comment}]
  [:div {:id    (format "ubinote-comment-%d" id)
         :class "bg-white mb-2 border-top border-dark pt-1"}
   [:div {:class "d-flex justify-content-between"}
    [:p {:class "fw-bold mb-0" :style "font-size: 0.8rem;"} creator_email]
    [:p {:class "fw-bold mb-0" :style "font-size: 0.8rem;"}
     (str (u/format-milliseconds (- (u/->millis-from-epoch (t/local-date-time))
                                  (u/->millis-from-epoch created_at)) :relative true)
      " ago")]]
   [:p {:class "mb-0" :style "white-space: pre-wrap;"} content]])

(defn create-comment
  [id {:keys [params] :as _req}]
  (->> (assoc (dissoc params :id)
              :annotation_id id
              :creator_id    api.u/*current-user-id*)
       (api.u/validate NewComment)
       (tc/insert-returning-instance! :m/comment)
       (merge {:creator_email (:email @api.u/*current-user*)})
       (ui/render :comment)
       ui/hiccup->html-response))

(defn- delete-annotation
  [id _req]
  (api.u/check-404 (tc/select-one :m/annotation :id id))
  (tc/delete! :m/annotation :id id)
  api.u/generic-200-response)

(defroutes routes
  (POST "/" [] create-annotation)
  (context "/:id" [id :<< as-int]
           (POST  "/comment" [] (partial create-comment id))
           (DELETE "/" [] (partial delete-annotation id))))
