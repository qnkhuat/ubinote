(ns ubinote.api.user
  (:require [compojure.core :refer [context defroutes GET POST]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models.user :refer [User]]
            [ubinote.models.common.schemas :as schemas]
            [cemerick.friend.credentials :as creds]
            [schema.core :as s]
            [toucan.db :as db]))

(def NewUser
  {:email      schemas/EmailAddress
   :first_name schemas/NonBlankString
   :last_name  schemas/NonBlankString
   :password   schemas/Password})

(def ^:private validate-create-user
  "Schema for creating a user"
  ;; TODO: are we logging user password out if it's invalid?
  (s/validator NewUser))

(defn create-user
  [{:keys [params] :as _req}]
  (validate-create-user params)
  (db/insert! User (assoc params :password (creds/hash-bcrypt (:password params)))))

(defn get-user
  [id _req]
  (-> (db/select-one User :id id)
      api/check-404))

(defn list-users
  [_req]
  (db/select User))

(defn current-user
  [req]
  (:currrent-user req))

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (GET "/current" [] current-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))))
