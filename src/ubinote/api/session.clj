(ns ubinote.api.session
  (:require [compojure.core :refer [defroutes POST]]
            [cemerick.friend.credentials :as creds]
            [ubinote.api.common :as api]
            [ubinote.models.common.schemas :as schemas]
            [ubinote.models.session :refer [Session]]
            [ubinote.models.user :refer [default-user-columns]]
            [ubinote.server.middleware.session :as mw.session]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewSession
  {:email    schemas/EmailAddress
   :password schemas/Password})

(defn verify-user
  [email password]
  (let [user (db/select-one ['User :id :email :password] :email email)]
    (api/check-404 user {:message "User not found"})
    (api/check-401 (creds/bcrypt-verify password (:password user)))
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

(defroutes routes
  (POST "/" [] create-session))
