(ns ubinote.server.routes
  (:require
   [compojure.core :refer [context defroutes GET]]
   [compojure.route :as route]
   [ring.util.response :refer [resource-response]]
   [ubinote.api.routes :as api.routes]
   [ubinote.api.util :as api.u]
   [ubinote.ui.page.core :as ui.page]))

;; TODO: should be a middleware
(defn require-login
  [handler req]
  (if (nil? api.u/*current-user-id*)
    ui.page/unauthorized
    (if (fn? handler)
      (handler req)
      handler)))

(defroutes routes
  (GET "/health" _req "fine 😁")
  (context "/api" _req api.routes/routes)
  (GET "/build/bundle.js" _req (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" _req (resource-response "frontend/build/bundle.css"))
  ;; let svelte handles it from here
  #_(GET "*" _req (resource-response "frontend/index.html"))

  ;; new page system
  (GET "/" req (require-login ui.page/index req))
  (GET "/login" _req ui.page/login)
  (route/not-found ui.page/not-found))
