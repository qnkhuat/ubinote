(ns ubinote.api.user
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes GET POST]]
   [toucan2.core :as tc]
   [ubinote.api.common :as api]
   [ubinote.models.common.schema :as schema]))

(def NewUser
  [:map
   [:email      schema/EmailAddress]
   [:first_name schema/NonBlankString]
   [:last_name  schema/NonBlankString]
   [:password   schema/Password]])

(defn create-user
  [{:keys [body] :as _req}]
  (api/validate NewUser body)
  ;; TODO: catch exception when create duplicate users
  (tc/insert! :m/user body))

(defn get-user
  [id _req]
  (-> (tc/select-one :m/user :id id)
      api/check-404))

(defn list-users
  [_req]
  (tc/select :m/user))

(defn current-user
  [_req]
  (api/check-404 @api/*current-user*))

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (GET "/current" [] current-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))))
