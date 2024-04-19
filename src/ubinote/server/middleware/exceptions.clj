(ns ubinote.server.middleware.exceptions
  (:require
   [clojure.tools.logging :as log]
   [ubinote.ui.page :as ui.page]))

(defn wrap-api-exceptions
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of error."
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception e
       ;; TODO: mask the value for schemas error, because it mays contain user's password
       (let [{:keys [status-code error-data]} (ex-data e)
             error-message                    (ex-message e)
             resp                             (cond
                                               (and status-code (or error-message error-data))
                                               (ui.page/error (format "%d: Error: %s" status-code (or error-message "Unknown error")))

                                               :else
                                               (do
                                                (log/error "Unknown error in api" e)
                                                (ui.page/error "500: Unknown exception in api")))]
         resp)))))
