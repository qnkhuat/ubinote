(ns ubinote.api.user
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes DELETE GET POST]]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.models.common.schema :as schema]
   [ubinote.ui :as ui]))

(def NewUser
  [:map
   [:email      schema/EmailAddress]
   [:first_name schema/NonBlankString]
   [:last_name  schema/NonBlankString]
   [:password   schema/Password]])

(defn create-user
  [{:keys [params] :as _req}]
  (api.u/decode NewUser params)
  (tc/insert! :m/user params)
  (api.u/htmx-trigger api.u/generic-200-response "trigger-list-user"))

(defn get-user
  [id _req]
  (-> (tc/select-one :m/user :id id)
      api.u/check-404))

(defmethod ui/render :users-table
  [_component users]
  [:table {:class "table table-hover"}
   [:thead
    [:tr
     [:th "ID"]
     [:th "Email"]
     [:th "Name"]
     [:th "Joined date"]
     [:th "Delete"]]]
   [:tbody {:hx-confirm "Are you sure?"
            :hx-swap    "outerHTML"
            :hx-target  "closest tr"}
    (for [user users]
      [:tr {:class "page-row"}
       [:td (:id user)]
       [:td (:email user)]
       [:td (str (:first_name user) " " (:last_name user))]
       [:td (str (:created_at user))]
       [:td [:button {:hx-delete (format "/api/user/%d" (:id user))
                      :class     "btn btn-danger"}
             [:i {:class "bi bi-trash"}]]]])]])

(defn- list-users
  [_req]
  (->> (tc/select :m/user {:order-by [[:created_at :asc]]})
       (ui/render :users-table)
       ui/render-hiccup-fragment))

(defn current-user
  [_req]
  (api.u/check-404 @api.u/*current-user*))

(defn delete-user
  [id _req]
  (api.u/check-404 (tc/select-one :m/user id))
  (tc/delete! :m/user id)
  api.u/generic-200-response)

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (GET "/current" [] current-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))
           (DELETE "/" [] (partial delete-user id))))
