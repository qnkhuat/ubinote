(ns ubinote.api.user
  (:require [compojure.coercions :refer [as-int]]
            [compojure.core :refer [context defroutes GET POST]]
            [toucan.db :as db]
            [ubinote.api.common :as api]
            [ubinote.models :refer [User]]
            [ubinote.models.common.schema :as schema]))

(def NewUser
  [:map
   [:email      schema/EmailAddress]
   [:first_name schema/NonBlankString]
   [:last_name  schema/NonBlankString]
   [:password   schema/Password]])

(defn create-user
  [{:keys [body] :as _req}]
  (schema/validate-schema body NewUser)
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
