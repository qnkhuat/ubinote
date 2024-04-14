(ns ubinote.api.comment
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes GET PUT POST]]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.ui :as ui]
   [ubinote.util :as u]))

(defmethod ui/render :comment-edit
  [_component {:keys [id content user created_at] :as _comment}]
  [:form {:id        (format "ubinote-comment-%d" id)
          :class     "bg-white mb-2 border-top border-dark pt-2"
          :hx-target "this"
          :hx-swap   "outerHTML"}
   [:div {:class "d-flex justify-content-between"}
    [:p {:class "fw-semibold mb-0"
         :style {:font-size "0.8rem"}} (:email user)]
    [:div
     [:span {:class "fw-semibold pe-1"
             :style {:font-size "0.8rem"}}
      (u/timestamp->ago-text created_at)]
     [:span {:class  "fw-semibold text-clickable"
             :hx-put (format "/api/comment/%d" id)
             :style  {:font-size "0.8rem"}} "Save"]]]
   [:div
    [:textarea {:class "w-100 mb-2"
                :style {:white-space :pre-wrap}
                :name  "content"}
     content]]])

(defmethod ui/render :comment
  [_component {:keys [id content creator_email user created_at] :as _comment}]
  [:div {:id        (format "ubinote-comment-%d" id)
         :class     "bg-white mb-2 border-top border-dark pt-2"
         :hx-target "this"
         :hx-swap   "outerHTML"}
   [:div {:class "d-flex justify-content-between"}
    [:p {:class "fw-semibold mb-0"
         :style {:font-size "0.8rem"}} (or creator_email (:email user))]
    [:div
     [:span {:class "fw-semibold pe-1"
             :style {:font-size "0.8rem"}}
      (u/timestamp->ago-text created_at)]
     [:span {:class "fw-semibold text-clickable"
             :style {:font-size "0.8rem"}
             :hx-get (format "/api/comment/%d/edit" id)} "Edit"]]]
   [:p {:class "mb-0" :style "white-space: pre-wrap;"} content]])

(defn update-comment
  [id {:keys [params] :as _req}]
  (let [{:keys [content]} params]
    (tc/update! :m/comment id {:content content})
    (->> (api.u/check-404 (tc/hydrate (tc/select-one :m/comment id) :user))
         (ui/render :comment)
         ui/render-hiccup-fragment)))

(defn edit-comment-form
  [id _req]
  (->> (api.u/check-404 (tc/hydrate (tc/select-one :m/comment id) :user))
       (ui/render :comment-edit)
       ui/render-hiccup-fragment))

(def NewComment
  [:map
   [:annotation_id :int]
   [:creator_id    :int]
   [:content       :string]])

(defn create-comment
  [{:keys [params] :as _req}]
  (->> (assoc params
              :creator_id api.u/*current-user-id*)
       (api.u/decode NewComment)
       (tc/insert-returning-instance! :m/comment)
       (merge {:creator_email (:email @api.u/*current-user*)})
       (ui/render :comment)
       ui/render-hiccup-fragment))

(defroutes routes
 (POST  "/" [] (partial create-comment))
 (context "/:id" [id :<< as-int]
          (PUT "/" [] (partial update-comment id))
          (GET "/edit" [] (partial edit-comment-form id))))
