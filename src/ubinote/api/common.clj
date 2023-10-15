(ns ubinote.api.common
  (:require
   [malli.core :as mc]
   [malli.error :as me]
   [malli.experimental.describe :as md]))

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
                                                 {:errors errors})))))))

(defn check-401
  "Return Unauthorized if test failed."
  ([x]
   (check-401 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Unauthorized." (merge {:status-code 401}
                                            (when errors
                                              {:errors errors})))))
   x))

(defn check-404
  "Return Not found if test failed."
  ([x]
   (check-404 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Not found." (merge {:status-code 404}
                                         (when errors
                                           {:errors errors})))))
   x))

(defn validate-schema
  "Throw an error if value does not match schema, else returns value."
  [value schema]
  (if-let [error (me/humanize (mc/explain schema value))]
    (throw (ex-info "Invalid value" {:status-code  400
                                     :schema/error error
                                     :describe     (md/describe schema)}))
    value))

(def generic-204-response
  {:status 204 :body nil})
