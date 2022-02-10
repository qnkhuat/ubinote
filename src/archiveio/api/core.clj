(ns archiveio.api.core
  (:require [compojure.core :refer [defroutes GET context]]
            [archiveio.api.archive :as archive]))

(defroutes routes
  (GET "/health" [] "api is fine")
  (context "/archive" [] archive/routes))
