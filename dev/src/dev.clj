(ns dev
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [archiveio.core :refer [app]]
            [archiveio.db :as adb]
            [archiveio.migration :as am]))

(defonce ^:private instance* (atom nil))

(defn start! []
  (println "Serving at localhost: 3000" )
  (adb/setup-db! ".archiveio")
  (am/migrate!)
  (reset! instance* (run-jetty (wrap-reload #'app) {:port 3000
                                                    :join? false})))

(defn stop! []
  (when @instance*
    (.stop @instance*)
    (reset! instance* nil)))

(defn restart! []
  (stop!)
  (start!))

(defn -main []
  (start!))
