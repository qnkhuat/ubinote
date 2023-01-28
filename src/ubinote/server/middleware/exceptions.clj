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
        (let [{:keys [status-code errors], :as info} #p (ex-data e)
              body                                   (cond
                                                       ;; If status code was specified but other data wasn't, it's something like a
                                                       ;; 404. Return message as the (plain-text) body.
                                                       (= [:status-code] (keys errors))
                                                       (.getMessage e)

                                                       ;; sometimes we throw like {:status-code 400 :errors "Failed to fetch"}
                                                       (and status-code errors)
                                                       {:message (.getMesage e)
                                                        :errors  errors}

                                                       ;; invalid API schema
                                                       (and status-code (:schema/error info))
                                                       {:message  (:schema/error info)
                                                        :describe (:describe info)}

                                                       ;; Otherwise return the full `Throwable->map` representation with Stacktrace
                                                       ;; and ex-data
                                                       :else
                                                       (do
                                                         (log/error e)
                                                         {:errors (merge
                                                                    (Throwable->map e)
                                                                    {:message (.getMessage e)}
                                                                    info)}))
              status-code (or status-code
                              500)]
          {:status status-code
           :body   body})))))
