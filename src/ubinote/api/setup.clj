(ns ubinote.api.setup
  (:require
   [compojure.core :refer [defroutes POST]]
   [ubinote.api.session :as api.session]
   [ubinote.api.user :as api.user]
   [ubinote.config :as cfg]))

(defn create-setup-user
  [req]
  (if (cfg/setup?)
    (throw (ex-info "App was already setup." {:status-code 400}))
    (do (api.user/create-user req)
        (api.session/create-session req))))

(defroutes routes
  (POST "/" [] create-setup-user))
