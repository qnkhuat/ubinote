(ns archiveio.api.response
  (:require [slingshot.slingshot :refer [try+]]))

(defn entity-response
  "Structure a response for a single entity"
  [resp-code data]
  {:status resp-code
   :body   data})

(defn error-response
  "Structure a response for an error"
  [resp-code errors]
  {:status resp-code
   :body   {:errors errors}})

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

(defn wrap-error-response
  "Run the wrapped code and override with a 400 error response for schema validation error
  or other specified types of errors."
  [handler]
  (fn [request]
    (try+
     (handler request)
     (catch [:type ::bad-parameter] e
       (error-response 400 {(:field e) (:message &throw-context)}))
     (catch [:type ::not-found] _
       {:status 404, :body {:error (:message &throw-context)}}))))
