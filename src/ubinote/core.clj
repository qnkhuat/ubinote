(ns ubinote.core
  (:require [ubinote.server.core :as server]))

(defn -main []
  (server/start! server/app))
