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
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

;; ensure we use a `BasicContextSelector` instead of a `ClassLoaderContextSelector` for log4j2. Ensures there is only
;; one LoggerContext instead of one per classpath root. Practical effect is that now `(LogManager/getContext true)`
;; and `(LogManager/getContext false)` will return the same (and only)
;; LoggerContext. https://logging.apache.org/log4j/2.x/manual/logsep.html
(System/setProperty "log4j2.contextSelector" "org.apache.logging.log4j.core.selector.BasicContextSelector")

;; ensure the [[clojure.tools.logging]] logger factory is the log4j2 version (slf4j is far slower and identified first)
(System/setProperty "clojure.tools.logging.factory" "clojure.tools.logging.impl/log4j2-factory")
(System/setProperty "log4j2.configurationFile" "log4j2.xml")

(defroutes routes
  ;; serving bundle.js this seems hacky?
  (GET "/build/bundle.js" [_req] (resource-response "frontend/build/bundle.js")) ;; inside the resources folder
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/files "/static" {:root page/root})
  ;; let's React handle it from here
  (GET "*" [_req] (resource-response "frontend/index.html"))) ;; inside the resources folder

(def app (middleware/apply-middleware routes middleware/middlewares))

(defn start!
  [app]
  (log/info "Starting server at localhost:" (cfg/config-int :port))
  (adb/setup-db!)
  (am/migrate!)
  (run-jetty app
             {:port  (cfg/config-int :port)
              :join? false}))
