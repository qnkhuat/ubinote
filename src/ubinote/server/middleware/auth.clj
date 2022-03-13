(ns ubinote.server.middleware.auth
  (:require [ubinote.api.common :as api]))

(defn +auth
  "Enforce a route to be authenticated"
  [handler]
  (fn [req]
    (api/check-401 (:current-user req))
    (handler req)))
