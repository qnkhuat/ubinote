(ns ubinote.server.middleware.exceptions
  (:require [clojure.tools.logging :as log]))

(defn- explain-one-schema-error
  [[k v]]
  [k (cond
       (= v 'missing-required-key) "Missing required key"
       :else "Invalid value")])

(defn explain-schema-error
  "Convert schema error to simple key -> message format"
  [errors]
  (try
    (into {} (map explain-one-schema-error errors))
    (catch Exception _
      ;; Catch-all for all kinds of other errors we don't currently handle (e.g. not an object)
      ;; This happens when we hit ^:always-validate deeper in the code triggering a schema error
      ;; rather than when we explicitly do schema validation at API input layer.
      {:unknown (pr-str errors)})))

(defn wrap-api-exceptions
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of error."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        ;; TODO: mask the value for schemas error, because it mays contain user's password
        (let [{:keys [status-code errors], :as info} (ex-data e)
              body                                   (cond
                                                       ;; If status code was specified but other data wasn't, it's something like a
                                                       ;; 404. Return message as the (plain-text) body.
                                                       (and status-code (empty? errors))
                                                       (.getMessage e)

                                                       ;; sometimes we throw like {:status-code 400 :errors "Failed to fetch"}
                                                       (and status-code errors)
                                                       errors

                                                       (= :schema.core/error (:type info))
                                                       {:errors (explain-schema-error (:error info))}

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
                              (when (= :schema.core/error (:type info))
                                400)
                              500)]
          {:status status-code
           :body   body})))))
