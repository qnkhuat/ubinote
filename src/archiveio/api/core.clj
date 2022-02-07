(ns archiveio.api.core
  (:require [compojure.core :refer [defroutes GET]]))

(defroutes routes
  (GET "/health" [] "api is fine"))
