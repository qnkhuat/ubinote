(ns archiveio.core
  (:require [archiveio.api.core :as api]
            [archiveio.db :as adb]
            [archiveio.migration :as am]
            [archiveio.api.response :as resp]
            [archiveio.config :as cfg]
            [clojure.string :as string]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/not-found "<h1>Page not found</h1>"))

(defn wrap-json-params-normalize
  "wrap-json-parms but with underscores->dashes"
  [handler]
  (wrap-json-params handler {:key-fn (fn [k]
                                       (string/replace k "_" "-"))}))

(defn wrap-json-response-normalize
  "wrap-json-response but with normalize dashes->underscores"
  [handler]
  (wrap-json-response handler {:key-fn (fn [k]
                                         (string/replace (name k) "-" "_"))}))

(def middlewares
  ;; middleware will be applied from bottom->top
  [
   resp/wrap-error-response
   wrap-json-response-normalize ; normalize response to json form
   wrap-keyword-params          ; normalizes string keys in :params to keyword keys
   wrap-json-params-normalize   ; extracts json POST body and makes it avaliable on request
   wrap-params                  ; parses GET and POST params as :query-params/:form-params and both as :params
   ])

(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))

(def app
  (apply-middleware routes middlewares))

(defn start-server
  ([]
   (adb/setup-db!)
   (am/migrate!)
   (run-jetty app
              {:port  (cfg/config-int :archiveio-port)
               :join? false})))


(defn -main []
  (start-server))
