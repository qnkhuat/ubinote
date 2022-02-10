(ns archiveio.core
  (:require [archiveio.api.core :as api]
            [archiveio.db :as adb]
            [archiveio.migration :as am]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [clojure.string :as string]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/not-found "<h1>Page not found</h1>"))

(defn wrap-json-params-convert
  "wrap-json-parms but with underscores->dashes"
  [req]
  (wrap-json-params req {:key-fn (fn [k]
                                   (string/replace k "_" "-"))}))

(defn wrap-json-response-convert
  "wrap-json-response but with convert dashes->underscores"
  [req] (wrap-json-response req
                            {:key-fn (fn [k]
                                       (string/replace (name k) "-" "_"))}))
(def middlewares
  ;; middleware will be applied from bottom->top
  [wrap-json-response-convert ; convert response to json form
   wrap-keyword-params        ; converts string keys in :params to keyword keys
   wrap-json-params-convert   ; extracts json POST body and makes it avaliable on request
   wrap-params                ; parses GET and POST params as :query-params/:form-params and both as :params
   ])

(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))

(def app
  (apply-middleware routes middlewares))

(defn -main []
  (adb/setup-db! ".archiveio")
  (am/migrate!)
  (run-jetty app {:port 3000
                  :join? false}))
