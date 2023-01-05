(ns ubinote.api.user
  (:require [compojure.core :refer [context defroutes GET POST]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models :refer [User]]
            [ubinote.models.common.schemas :as schemas]
            [schema.core :as s]
            [toucan.db :as db]))

(def NewUser
  {:email      schemas/EmailAddress
   :first_name schemas/NonBlankString
   :last_name  schemas/NonBlankString
   :password   schemas/Password})

(def ^:private validate-create-user
  "Schema for creating a user"
  (s/validator NewUser))

(defn create-user
  [{:keys [body] :as _req}]
  (validate-create-user body)
  ;; TODO: catch exception when create duplicate users
  (db/insert! User body))

(defn get-user
  [id _req]
  (-> (db/select-one User :id id)
      api/check-404))

(defn list-users
  [_req]
  (db/select User))

(defn current-user
  [_req]
  (api/check-404 @api/*current-user*))

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (GET "/current" [] current-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))))
