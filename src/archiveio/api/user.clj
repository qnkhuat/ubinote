(ns archiveio.api.user
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.api.response :as resp]
            [archiveio.model.user :refer [User]]
            [archiveio.controller.user :as user]
            [schema.core :as s]
            [toucan.db :as db]))

(def ^:private validate-create-user
  "Schema for adding a user"
  (s/validator user/NewUser))

(defn create-user
  [{:keys [params] :as _req}]
  (validate-create-user params)
  (resp/entity-response 200 (user/create params)))

(defn get-user
  [id _req]
  (let [user (db/select-one User :id id)]
    (resp/assert-404 user "User not found")
    (resp/entity-response 200 user)))

(defn list-users
  [_req]
  (resp/entity-response 200 (db/select User)))

(defroutes routes
  (GET "/" [] list-users)
  (POST "/" [] create-user)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-user id))))