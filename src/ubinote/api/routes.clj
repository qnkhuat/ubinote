(ns ubinote.api.routes
  (:require
   [compojure.core :refer [defroutes GET context]]
   [ubinote.api.annotation :as api.annotation]
   [ubinote.api.comment :as api.comment]
   [ubinote.api.page :as api.page]
   [ubinote.api.public :as api.public]
   [ubinote.api.session :as api.session]
   [ubinote.api.setup :as api.setup]
   [ubinote.api.user :as api.user]
   [ubinote.server.middleware.auth :refer [+auth]]))

(defroutes routes
  (GET "/health" []         "Doing great!")
  (context "/page" []       (+auth api.page/routes))
  (context "/user" []       (+auth api.user/routes))
  (context "/annotation" [] (+auth api.annotation/routes))
  (context "/comment" []    (+auth api.comment/routes))
  (context "/public" []     api.public/routes)
  (context "/session" []    api.session/routes)
  (context "/setup" []      api.setup/routes))
