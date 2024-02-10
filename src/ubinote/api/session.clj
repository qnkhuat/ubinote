(ns ubinote.api.session
  (:require
   [compojure.core :refer [defroutes DELETE POST GET]]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.config :as cfg]
   [ubinote.models.common.schema :as schema]
   [ubinote.models.user :refer [default-user-columns]]
   [ubinote.server.middleware.session :as mw.session]
   [ubinote.util.password :as passwd]))

(def NewSession
  [:map
   [:email    schema/EmailAddress]
   [:password schema/Password]])

(defn verify-user
  [email password]
  (let [user (tc/select-one [:m/user :id :email :password :password_salt] :email email)]
    (api.u/check-404 user {:message "User not found"})
    (api.u/check-401 (passwd/verify-password password (:password_salt user) (:password user)))
    (select-keys user default-user-columns)))

(defn create-session
  [{:keys [params] :as req}]
  (api.u/validate NewSession params)
  (let [user    (api.u/check-401 (verify-user (:email params) (:password params)))
        session {:id (first (tc/insert-returning-pks! :m/session {:user_id (:id user)}))}]
    (-> req
        (mw.session/set-session-cookie {} session)
        (api.u/htmx-redirect "/"))))

(defn delete-session
  [req]
  (let [session-id (:ubinote-session-id req)]
    (api.u/check-404 (tc/select-one :m/session :id session-id))
    (tc/delete! :m/session :id session-id)
    (mw.session/clear-session-cookie api.u/generic-204-response)))

(defn session-properties
  [_req]
  {:has_user_setup (cfg/setup?)})

(defroutes routes
  (POST "/" [] create-session)
  (DELETE "/" [] delete-session)
  (GET "/properties" [] session-properties))
