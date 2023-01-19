(ns dev
  (:require
    [ubinote.server :as server]))

(defonce ^:private instance* (atom nil))

(defn start! []
  (reset! instance* (server/start! #'server/app)))

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
