(ns ubinote.api.common
  (:require
   [malli.core :as mc]
   [malli.error :as me]))

;; the value of these dynamics var will be bind by [[ubinote.server.middleware.session/wrap-current-user-info]]
(def ^:dynamic *current-user-id* nil)
(def ^:dynamic *current-user*
  "Delay that returns the `User` (or nil) associated with the current API call.
  ex. `@*current-user*`"
  (atom nil))


;; ---------------------------------------- Define endpoint fns ----------------------------------------


;; ---------------------------------------- check fns ----------------------------------------
(defn check-400
  "Return Invalid Request if test failed."
  ([x]
   (check-400 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Invalid request." (merge {:status-code 400}
                                               (when errors
                                                 {:error-message errors})))))))

(defn check-401
  "Return Unauthorized if test failed."
  ([x]
   (check-401 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Unauthorized." (merge {:status-code 401}
                                            (when errors
                                              {:error_message errors})))))
   x))

(defn check-404
  "Return Not found if test failed."
  ([x]
   (check-404 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Not found." (merge {:status-code 404}
                                         (when errors
                                           {:error-message errors})))))
   x))

(defn validate
  "Throw an error if value does not match schema, else returns value."
  [schema value]
  (if-let [error (me/humanize (mc/explain schema value))]
    (throw (ex-info "Invalid value" {:status-code  400
                                     :error-message  "Data input does not match schema"
                                     :error-data     error}))
    value))

(def generic-204-response
  {:status 204 :body nil})
