(ns ubinote.api.core
  (:require [compojure.core :refer [defroutes GET context]]
            [ubinote.api.page :as page]
            [ubinote.api.annotation :as ant]
            [ubinote.api.comment :as cmt]
            [ubinote.api.user :as user]))

(defroutes routes
  (GET "/health" [] "api is fine")
  (context "/page" [] page/routes)
  (context "/user" [] user/routes)
  (context "/annotation" [] ant/routes)
  (context "/comment" [] cmt/routes))
