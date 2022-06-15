(ns ubinote.server.core
  (:require [ubinote.api.core :as api]
            [ubinote.server.db :as adb]
            [ubinote.migration :as am]
            [ubinote.config :as cfg]
            [ubinote.models.page :as page]
            [ubinote.server.middleware.core :as middleware]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]
            [ring.util.response :refer [resource-response]]
            [taoensso.timbre :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes routes
  ;; serving bundle.js this seems hacky?
  (GET "/static/js/bundle.js" [_req] (resource-response "frontend/static/js/bundle.js")) ;; inside the resources folder
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/files "/static" {:root page/root})
  ;; let's React handle it from here
  (GET "*" [_req] (resource-response "frontend/index.html"))) ;; inside the resources folder

(def app (middleware/apply-middleware routes middleware/middlewares))

(defn start!
  [app]
  (log/info "Starting server at localhost:" (cfg/config-int :un-port))
  (adb/setup-db!)
  (am/migrate!)
  (run-jetty app
             {:port  (cfg/config-int :un-port)
              :join? false}))
