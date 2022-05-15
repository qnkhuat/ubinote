(ns ubinote.server.core
  (:require [ubinote.api.core :as api]
            [ubinote.server.db :as adb]
            [ubinote.migration :as am]
            [ubinote.server.middleware.core :as middleware]
            [ubinote.config :as cfg]
            [ubinote.models.page :as page]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]
            [taoensso.timbre :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/files "/static" {:root page/root})
  (route/not-found "<h1>Page not found</h1>"))

(def app (middleware/apply-middleware routes middleware/middlewares))

(defn start!
  [app]
  (log/info "Starting server at localhost:" (cfg/config-int :un-port))
  (adb/setup-db!)
  (am/migrate!)
  (run-jetty app
             {:port  (cfg/config-int :un-port)
              :join? false}))
