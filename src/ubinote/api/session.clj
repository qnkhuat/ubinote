(ns ubinote.api.session
  (:require [compojure.core :refer [defroutes POST DELETE]]
            [ubinote.controller.session :as session]
            [ubinote.api.common :as api]
            [ubinote.model.common.schemas :as schemas]
            [schema.core :as s]))

(def NewSession
  {:username schemas/Username
   s/Keyword s/Str})

(def ^:private validate-create-session
  "Schema for creating session"
  (s/validator NewSession))

(defn create-session
  [{:keys [params] :as _req}]
  (validate-create-session params)
  (if-let [user (session/verify-user (:username params) (:password params))]
    (select-keys (session/create (:id user)) [:id])
    (api/check-401 false)))

(defroutes routes
  (POST "/" [] create-session))
