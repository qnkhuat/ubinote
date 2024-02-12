(ns ubinote.server.routes
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes GET]]
   [compojure.route :as route]
   [ring.util.response :as response :refer [resource-response]]
   [ubinote.api.routes :as api.routes]
   [ubinote.api.util :as api.u]
   [ubinote.ui.page.core :as ui.page]))

;; TODO: should be a middleware
(defn require-login
  [handler]
  (if (nil? api.u/*current-user-id*)
    ui.page/unauthorized
    (if (fn? handler)
      (handler)
      handler)))

(defroutes routes
  (GET "/health" _req "fine 😁")
  (context "/api" _req api.routes/routes)
  (GET "/build/bundle.js" _req (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" _req (resource-response "frontend/build/bundle.css"))
  (GET "/static/:file" [file] (resource-response (format "ui/static/%s" file)))
  ;; let svelte handles it from here
  #_(GET "*" _req (resource-response "frontend/index.html"))

  ;; new page system
  (GET "/" _req (require-login ui.page/index))
  (GET "/page/:id" [id :<< as-int] (require-login (partial ui.page/view-page id)))
  (GET "/login" _req ui.page/login)
  (route/not-found ui.page/not-found))

(response/content-type (resource-response (format "ui/static/%s" "app.js")))
