(ns ubinote.server.core
  (:require
    [clojure.tools.logging :as log]
    [ring.adapter.jetty :refer [run-jetty]]
    [ubinote.config :as cfg]
    [ubinote.migration :as am]
    [ubinote.server.db :as adb]
    [ubinote.server.middleware.core :as middleware]
    [ubinote.server.routes :as routes]))

;; ensure we use a `BasicContextSelector` instead of a `ClassLoaderContextSelector` for log4j2. Ensures there is only
;; one LoggerContext instead of one per classpath root. Practical effect is that now `(LogManager/getContext true)`
;; and `(LogManager/getContext false)` will return the same (and only)
;; LoggerContext. https://logging.apache.org/log4j/2.x/manual/logsep.html
(System/setProperty "log4j2.contextSelector" "org.apache.logging.log4j.core.selector.BasicContextSelector")

;; ensure the [[clojure.tools.logging]] logger factory is the log4j2 version (slf4j is far slower and identified first)
(System/setProperty "clojure.tools.logging.factory" "clojure.tools.logging.impl/log4j2-factory")
(System/setProperty "log4j2.configurationFile" "log4j2.xml")

(def app (middleware/apply-middleware routes/routes middleware/middlewares))

(defn start!
  [app]
  (log/info "Starting server at localhost:" (cfg/config-int :port))
  (adb/setup-db!)
  (am/migrate!)
  (run-jetty app
             {:port  (cfg/config-int :port)
              :join? false}))
