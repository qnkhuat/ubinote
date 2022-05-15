(ns ubinote.api.session
  (:require [compojure.core :refer [defroutes POST]]
            [cemerick.friend.credentials :as creds]
            [ubinote.api.common :as api]
            [ubinote.model.common.schemas :as schemas]
            [ubinote.model.session :refer [Session]]
            [ubinote.model.user :refer [default-user-columns]]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewSession
  {:username schemas/Username
   s/Keyword s/Str})

(defn verify-user
  [username password]
  (let [user (db/select-one ['User :id :username :first_name :last_name :password :created_at :updated_at] :username username)]
    (api/check-404 user {:message "User not found"})
    (api/check-401 (creds/bcrypt-verify password (:password user)))
    (select-keys user default-user-columns)))

(def ^:private validate-create-session
  "Schema for creating session"
  (s/validator NewSession))

(defn create-session
  [{:keys [params] :as _req}]
  (validate-create-session params)
  (if-let [user (verify-user (:username params) (:password params))]
    (select-keys (db/insert! Session {:user-id user}) [:id])
    (api/check-401 false)))

(defroutes routes
  (POST "/" [] create-session))
