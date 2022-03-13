(ns ubinote.server.middleware.auth
  (:require [ubinote.api.common :as api]))

(defn +auth
  [handler]
  (fn [req]
    (api/check-401 (:un-user req))
    (handler req)))
