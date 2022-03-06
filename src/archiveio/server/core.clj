(ns archiveio.server.core
  (:require [archiveio.api.core :as api]
            [archiveio.server.db :as adb]
            [archiveio.migration :as am]
            [archiveio.server.middleware :as middleware]
            [archiveio.config :as cfg]
            [archiveio.controller.archive.path :as apath]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/files "/static" {:root apath/root})
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (middleware/apply-middleware routes middleware/middlewares))

(defn start!
  [app]
  (adb/setup-db!)
  (am/migrate!)
  (run-jetty app
             {:port  (cfg/config-int :aio-port)
              :join? false}))
