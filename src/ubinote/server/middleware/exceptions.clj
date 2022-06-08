(ns ubinote.server.middleware.exceptions
  (:require [taoensso.timbre :as log]))

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
   (catch clojure.lang.ExceptionInfo _
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
     (catch Throwable e
       ;; TODO: mask the value for schemas error, because it mays contain user's password
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

                                                (= :schema.core/error (:type info))
                                                {:errors (explain-schema-error (:error info))}

                                                ;; Otherwise return the full `Throwable->map` representation with Stacktrace
                                                ;; and ex-data
                                                :else
                                                {:errors (merge
                                                          (Throwable->map e)
                                                          {:message (.getMessage e)}
                                                          other-info)})]
         {:status  (or status 500)
          :body    body})))))
