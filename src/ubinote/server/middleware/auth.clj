(ns ubinote.server.middleware.auth
  (:require [ubinote.api.common :as api]
            [toucan.db :as db]))

(defn +auth
  "Enforce a route to be authenticated"
  [handler]
  (fn [req]
    ;; TODO: you know what to do
    ;(api/check-401 (:current-user req))
    (handler (assoc req :current-user (db/select-one 'User)))))
