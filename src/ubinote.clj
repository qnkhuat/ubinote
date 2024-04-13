(ns ubinote
  (:gen-class)
  (:require
   [ubinote.server :as server]))

(defn -main []
  (server/start! #'server/app))
