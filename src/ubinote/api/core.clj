(ns ubinote.api.core
  (:require [compojure.core :refer [defroutes GET context]]
            [ubinote.api.annotation :as ant]
            [ubinote.api.note :as note]
            [ubinote.api.page :as page]
            [ubinote.api.session :as session]
            [ubinote.api.setup :as setup]
            [ubinote.api.user :as user]
            [ubinote.server.middleware.auth :refer [+auth]]))

(defroutes routes
  (GET "/health" []         "Doing great!")
  (context "/page" []       (+auth page/routes))
  (context "/user" []       (+auth user/routes))
  (context "/annotation" [] (+auth ant/routes))
  (context "/note" []       (+auth note/routes))
  (context "/session" []    session/routes)
  (context "/setup" []      setup/routes))
