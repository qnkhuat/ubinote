(ns dev
  (:require
    [ring.middleware.reload :refer [wrap-reload]]
    [ubinote.server :as server]))

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
