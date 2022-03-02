(ns archiveio.api.core
  (:require [compojure.core :refer [defroutes GET context]]
            [archiveio.api.archive :as archive]
            [archiveio.api.annotation :as ant]
            [archiveio.api.comment :as cmt]
            [archiveio.api.user :as user]))

(defroutes routes
  (GET "/health" [] "api is fine")
  (context "/archive" [] archive/routes)
  (context "/user" [] user/routes)
  (context "/annotation" [] ant/routes)
  (context "/comment" [] cmt/routes))

