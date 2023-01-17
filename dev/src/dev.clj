(ns dev
  (:require [ubinote.server :as server]
            [ring.middleware.reload :refer [wrap-reload]]))

(defonce ^:private instance* (atom nil))

(defn start! []
  (server/start! (wrap-reload #'server/app)))

(defn stop! []
  (when @instance*
    (.stop @instance*)
    (reset! instance* nil)))

(defn restart! []
  (stop!)
  (start!)
  nil)

(defn -main []
  (start!))
