(ns dev
  (:require [archiveio.config :as cfg]
            [archiveio.core :refer [app]]
            [archiveio.db :as adb]
            [archiveio.migration :as am]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defonce ^:private instance* (atom nil))

(defn start! []
  (let [port (cfg/config-int :archiveio-port)]
    (println "Serving at localhost: 3000" )
    (adb/setup-db!)
    (am/migrate!)
    (reset! instance* (run-jetty (wrap-reload #'app) {:port  port
                                                      :join? false}))))

(defn stop! []
  (when @instance*
    (.stop @instance*)
    (reset! instance* nil)))

(defn restart! []
  (stop!)
  (start!))

(defn -main []
  (start!))
