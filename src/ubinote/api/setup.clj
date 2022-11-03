(ns ubinote.api.setup
  (:require [compojure.core :refer [defroutes POST]]
            [ubinote.config :as cfg]
            [ubinote.api.user :as api.user]))

(defn create-setup-user
  [req]
  (if (cfg/setup?)
    (throw (ex-info "App was already setup." {:status-code 400}))
    (api.user/create-user req)))

(defroutes routes
  (POST "/" [] create-setup-user))
