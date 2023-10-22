(ns ubinote.server.middleware.exceptions
  (:require [clojure.tools.logging :as log]))

(defn wrap-api-exceptions
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of error."
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception e
       ;; TODO: mask the value for schemas error, because it mays contain user's password
       (let [{:keys [status-code error-message error-data]} #p (ex-data e)
             body                                   (cond
                                                     (and status-code (or error-message error-data))
                                                     {:error_message (or error-message "Unknown error")
                                                      :error_data    (or error-data nil)}

                                                     :else
                                                     (do
                                                      (log/error "Unknown exception in api" e)
                                                      {:error_messsage "Internal error"
                                                       :error_data     nil}))
             status-code (or status-code
                             500)]
         {:status status-code
          :body   body})))))
