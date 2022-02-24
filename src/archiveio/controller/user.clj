(ns archiveio.controller.user
  (:require [archiveio.model.user :refer [User]]
            [archiveio.model.common.schemas :as schemas]
            [cemerick.friend.credentials :as creds]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewUser
  {:email      schemas/EmailAddress
   :first-name schemas/NonBlankString
   :last-name  schemas/NonBlankString
   :password   schemas/Password})

(s/defn create-user
  [new-user :- NewUser]
  (db/insert! User (assoc new-user :password (creds/hash-bcrypt (:password new-user)))))
