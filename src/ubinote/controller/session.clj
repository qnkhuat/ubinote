(ns ubinote.controller.session
  (:require [cemerick.friend.credentials :as creds]
            [ubinote.api.common :as api]
            [ubinote.model.session :refer [Session]]
            [ubinote.model.user :refer [default-user-columns]]
            [toucan.db :as db]))

(defn create
  [user-id]
  (db/insert! Session {:user-id user-id}))

(defn verify-user
  [username password]
  (let [user (db/select-one ['User :id :username :first-name :last-name :password :created-at :updated-at] :username username)]
    (api/check-404 user {:message "User not found"})
    (api/check-401 (creds/bcrypt-verify password (:password user)))
    (select-keys user default-user-columns)))
