(ns ubinote.api.core
  (:require [compojure.core :refer [defroutes GET context]]
            [ubinote.api.page :as page]
            [ubinote.api.annotation :as ant]
            [ubinote.api.comment :as cmt]
            [ubinote.api.user :as user]
            [ubinote.api.session :as session]
            [ubinote.server.middleware.auth :refer [+auth]]))

(defroutes routes
  (GET "/health" [] "api is fine")
  (context "/page" [] (+auth page/routes))
  (context "/user" [] (+auth user/routes))
  (context "/annotation" [] (+auth ant/routes))
  (context "/comment" [] (+auth cmt/routes))
  (context "/session" [] session/routes))
