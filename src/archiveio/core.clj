(ns archiveio.core
  (:require [archiveio.server.core :as server]))

(defn -main []
  (server/start! server/app))
