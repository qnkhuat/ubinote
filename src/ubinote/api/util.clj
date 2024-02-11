(ns ubinote.api.util
  (:require
   [clojure.string :as str]
   [hiccup2.core :as h]
   [malli.core :as mc]
   [malli.error :as me]
   [ring.util.response :as response]))

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
  ([x e-msg]
   (when-not x
     (throw (ex-info (or "Invalid request." e-msg) {:status-code 400})))
   x))

(defn check-401
  "Return Unauthorized if test failed."
  ([x]
   (check-401 x nil))
  ([x e-msg]
   (when-not x
     (throw (ex-info (or e-msg "Unauthorized") {:status-code 401})))
   x))

(defn check-404
  "Return Not found if test failed."
  ([x]
   (check-404 x nil))
  ([x e-msg]
   (when-not x
     (throw (ex-info (or "Not found." e-msg) {:status-code 404})))
   x))

(defn validate
  "Throw an error if value does not match schema, else returns value."
  [schema value]
  (if-let [error (me/humanize (mc/explain schema value))]
    (throw (ex-info "Invalid value" {:status-code  400
                                     :error-message  "Data input does not match schema"
                                     :error-data     error}))
    value))

(def generic-200-response
  {:status 200 :body nil})

(defn response?
  [x]
  (and (map? x)
       (contains? x :status)
       (int? (:status x))))

(defn ->response
  ([x]
   (->response x 200))
  ([x status]
   (if-not (response? x)
     {:status status
      :body   x}
     (assoc x :status status))))

(defn html
  "Render a hiccup html response."
  [resp]
  (-> (h/html {} (->response resp))
      str
      response/response
      (response/content-type "text/html")))

(defn htmx-redirect
  [resp url]
  (-> resp
      (->response 303)
      (response/header "HX-Redirect" url)))

(defn htmx-trigger
  [resp trigger]
  (assert (str/starts-with? trigger "trigger-") "trigger should start with trigger-")
  (-> resp
      ->response
      (response/header "HX-Trigger" trigger)))
