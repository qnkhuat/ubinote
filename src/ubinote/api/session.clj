(ns ubinote.api.session
  (:require
    [compojure.core :refer [defroutes DELETE POST GET]]
    [schema.core :as s]
    [toucan.db :as db]
    [ubinote.api.common :as api]
    [ubinote.config :as cfg]
    [ubinote.models.common.schemas :as schemas]
    [ubinote.models.session :refer [Session]]
    [ubinote.models.user :refer [default-user-columns]]
    [ubinote.server.middleware.session :as mw.session]
    [ubinote.util.password :as passwd]))

(def NewSession
  {:email    schemas/EmailAddress
   :password schemas/Password
   s/Keyword s/Any})

(defn verify-user
  [email password]
  (let [user (db/select-one ['User :id :email :password] :email email)]
    (api/check-404 user {:message "User not found"})
    (api/check-401 (passwd/bcrypt-verify password (:password user)))
    (select-keys user default-user-columns)))

(def ^:private validate-create-session
  "Schema for creating session"
  (s/validator NewSession))

(defn create-session
  [{:keys [body] :as req}]
  (validate-create-session body)
  (let [user    (api/check-401 (verify-user (:email body) (:password body)))
        session (select-keys (db/insert! Session {:creator_id (:id user)}) [:id])]
    (mw.session/set-session-cookie req {:body   session
                                        :status 200}
                                   session)))

(defn delete-session
  [req]
  (let [session-id (:ubinote-session-id req)]
    (api/check-404 (db/select-one Session :id session-id))
    (db/delete! Session :id session-id)
    (mw.session/clear-session-cookie api/generic-204-response)))

(defn session-properties
  [_req]
  {:has_user_setup (cfg/setup?)})

(defroutes routes
  (POST "/" [] create-session)
  (DELETE "/" [] delete-session)
  (GET "/properties" [] session-properties))
