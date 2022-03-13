(ns ubinote.controller.user
  (:require [ubinote.model.user :refer [User]]
            [ubinote.model.common.schemas :as schemas]
            [cemerick.friend.credentials :as creds]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewUser
  {:username   schemas/Username
   :first-name schemas/NonBlankString
   :last-name  schemas/NonBlankString
   :password   schemas/Password})

(s/defn create
  [new-user :- NewUser]
  (db/insert! User (assoc new-user :password (creds/hash-bcrypt (:password new-user)))))
