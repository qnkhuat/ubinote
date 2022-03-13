(ns ubinote.api.user
  (:require [compojure.core :refer [context defroutes GET POST]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.model.user :refer [User]]
            [ubinote.controller.user :as user]
            [schema.core :as s]
            [toucan.db :as db]))

(def ^:private validate-create-user
  "Schema for adding a user"
  (s/validator user/NewUser))

(defn create-user
  [{:keys [params] :as _req}]
  (validate-create-user params)
  (user/create params))

(defn get-user
  [id _req]
  (let [user (db/select-one User :id id)]
    (api/check-404 user)
    user))

(defn list-users
  [_req]
  (db/select User))

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))))
