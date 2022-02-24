(ns archiveio.api.response
  (:require [slingshot.slingshot :refer [try+]]
            [taoensso.timbre :as log]))

(defn assert-400
  "Throw ex-info which will result in 400 response if assertion fails"
  [x field message]
  (when-not x
    (throw (ex-info message
                    {:type  ::bad-parameter
                     :field field}))))

(defn assert-404
  "Throw ex-info which will result in 404 response if assertion fails"
  [x message]
  (when-not x
    (throw (ex-info message {:type ::not-found}))))

(defn entity-response
  "Structure a response for a single entity"
  [resp-code data]
  {:status resp-code
   :body   data})

(defn error-response
  "Structure a response for an error"
  [resp-code error]
  {:status resp-code
   :body   {:error error}})

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

(defn wrap-error-response
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of error."
  [handler]
  (fn [request]
    (try+
      (handler request)
      (catch [:type ::bad-parameter] e
        (log/error e)
        (error-response 400 {(:field e) (:message &throw-context)}))
      (catch [:type ::not-found] e
        (log/error e)
        (error-response  404 (:message &throw-context)))
      (catch [:type :schema.core/error] e
        (error-response 400 (explain-schema-error (:error e))))
      ;; shouldn't get to this point
      (catch AssertionError e
        (log/error e)
        (error-response 400 (ex-message e)))
      ;; catch all
      (catch Throwable e
        (log/error e)
        (error-response 500 "Internal Error")))))
