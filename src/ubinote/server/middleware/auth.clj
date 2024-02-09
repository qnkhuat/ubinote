(ns ubinote.server.middleware.auth
  (:require
   [ubinote.api.util :as api.u]))

(defn +auth
  "Enforce a route to be authenticated"
  [handler]
  (fn [req]
    (api.u/check-401 api.u/*current-user-id*)
    (handler req)))
