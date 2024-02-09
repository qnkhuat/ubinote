(ns ubinote.server.routes
  (:require
   [compojure.core :refer [context defroutes GET]]
   [compojure.route :as route]
   [ring.util.response :refer [resource-response]]
   [ubinote.api.common :as api.common]
   [ubinote.api.routes :as api.routes]
   [ubinote.server.middleware.session :as mw.session]
   [ubinote.ui.page.core :as ui.page]
   [ubinote.ui.template.core :as ui.template]))

;; TODO: should be a middleware
(defn require-login
  [handler req]
  (if (nil? (get-in req [:cookies mw.session/ubinote-session-cookie]))
    (api.common/html ui.template/unauthorized)
    (if (fn? handler)
      (handler req)
      handler)))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api.routes/routes)
  (GET "/build/bundle.js" [_req] (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" [_req] (resource-response "frontend/build/bundle.css"))
  ;; let svelte handles it from here
  (GET "old/*" [] (resource-response "frontend/index.html"))
  (GET "/" [req] (require-login ui.page/index req))
  (GET "/login" [_req] ui.page/login)
  (route/not-found "Not Found"))
