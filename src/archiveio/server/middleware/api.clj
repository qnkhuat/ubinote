(ns archiveio.server.middleware.api
  (:require [archiveio.api.common :refer [security-header]]
    [taoensso.timbre :as log]
            [ring.util.response :refer [response?]]))

(defn wrap-response-if-needed
  "This enable APIs to just return an object and it'll be automatically turned into a response.
  For error cases, we should return a proper response within the API"
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (if-not (response? resp)
        (if (instance? Object resp)
          {:status 200 :body resp}
          {:status 204 :body nil})
        resp))))

(defn wrap-api-exceptions
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of error."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (log/error e)
        (let [{:keys [status errors], :as info} (ex-data e)
              other-info                        (dissoc info :status)
              body                              (cond
                                                  ;; If status code was specified but other data wasn't, it's something like a
                                                  ;; 404. Return message as the (plain-text) body.
                                                  (and status (empty? other-info))
                                                  (.getMessage e)

                                                  ;; if the response includes `:errors`, (e.g., it's something like a generic
                                                  ;; parameter validation exception), just return the `other-info` from the
                                                  ;; ex-data.
                                                  (and status errors)
                                                  other-info

                                                  ;; Otherwise return the full `Throwable->map` representation with Stacktrace
                                                  ;; and ex-data
                                                  :else
                                                  (merge
                                                    (Throwable->map e)
                                                    {:message (.getMessage e)}
                                                    other-info))]
          {:status  (or status 500)
           :headers security-header
           :body    body})))))
