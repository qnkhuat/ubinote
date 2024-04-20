(ns ubinote.api.comment
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes DELETE GET PUT POST]]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.ui :as ui]
   [ubinote.ui.trigger :as ui.trigger]
   [ubinote.util :as u]))

(defmethod ui/render :annotation-comment-edit
  [_component {:keys [id content creator created_at] :as _comment}]
  [:form {:id        (format "ubinote-comment-%d" id)
          :class     "bg-white mb-2 border-top border-dark pt-2"
          :hx-target "this"
          :hx-swap   "outerHTML"}
   [:div {:class "d-flex justify-content-between"}
    [:p {:class "fw-semibold mb-0"
         :style {:font-size "0.8rem"}} (:email creator)]
    [:div
     [:span {:class "fw-semibold pe-1"
             :style {:font-size "0.8rem"}}
      (u/timestamp->ago-text created_at)]
     [:span {:class  "fw-semibold text-primary cursor-pointer text-decoration-underline pe-1"
             :hx-put (format "/api/comment/%d" id)
             :style  {:font-size "0.8rem"}} "Save"]
     [:span {:class      "fw-semibold text-danger cursor-pointer text-decoration-underline"
             :hx-confirm "Are you sure?"
             :hx-delete  (format "/api/comment/%d" id)
             :style      {:font-size "0.8rem"}} "Delete"]]]

   [:div
    [:textarea {:class "w-100 mb-2"
                :style {:white-space :pre-wrap}
                :name  "content"}
     content]]])

(defmethod ui/render :annotation-comment
  [_component {:keys [id content creator_email creator created_at] :as _comment}]
  [:div {:id        (format "ubinote-comment-%d" id)
         :class     "bg-white mb-2 border-top border-dark pt-2"
         :hx-target "this"
         :hx-swap   "outerHTML"}
   [:div {:class "d-flex justify-content-between"}
    [:p {:class "fw-semibold mb-0"
         :style {:font-size "0.8rem"}} (or creator_email (:email creator))]
    [:div
     [:span {:class "fw-semibold pe-1"
             :style {:font-size "0.8rem"}}
      (u/timestamp->ago-text created_at)]
     [:span {:class "fw-semibold text-primary cursor-pointer text-decoration-underline"
             :style {:font-size "0.8rem"}
             :hx-get (format "/api/comment/%d/edit" id)} "Edit"]]]
   [:p {:class "mb-0" :style "white-space: pre-wrap;"} content]])

(defn update-comment
  [id {:keys [params] :as _req}]
  (let [{:keys [content]} params]
    (tc/update! :m/comment id {:content content})
    (->> (api.u/check-404 (tc/hydrate (tc/select-one :m/comment id) :creator))
         (ui/render :annotation-comment)
         ui/render-hiccup-fragment)))

(defn edit-comment-form
  [id _req]
  (->> (api.u/check-404 (tc/hydrate (tc/select-one :m/comment id) :creator))
       (ui/render :annotation-comment-edit)
       ui/render-hiccup-fragment))

(def NewComment
  [:map
   [:annotation_id :int]
   [:creator_id    :int]
   [:content       :string]])

(defn create-comment
  [{:keys [params] :as _req}]
  (let [cmt (api.u/decode NewComment (assoc params
                                            :creator_id api.u/*current-user-id*))]
    (->> cmt
         (tc/insert-returning-instance! :m/comment)
         (merge {:creator_email (:email @api.u/*current-user*)})
         (ui/render :annotation-comment)
         ui/render-hiccup-fragment
         (ui.trigger/update-annotation-color-if-needed (:annotation_id cmt)))))

(defn delete-comment
  [id _req]
  (let [cmt (api.u/check-404 (tc/select-one :m/comment id))]
   (tc/delete! :m/comment id)
   (ui.trigger/update-annotation-color-if-needed (:annotation_id cmt) api.u/generic-200-response)))

(defroutes routes
  (POST  "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (DELETE "/" [] (partial delete-comment id))
           (PUT "/" [] (partial update-comment id))
           (GET "/edit" [] (partial edit-comment-form id))))
