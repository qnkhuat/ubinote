(ns ubinote.core
  (:require [ubinote.server :as server]))

(defn -main []
  (server/start! server/app))
