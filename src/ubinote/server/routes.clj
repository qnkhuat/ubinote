(ns ubinote.server.routes
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes GET]]
   [compojure.route :as route]
   [ring.util.response :as response :refer [resource-response]]
   [ubinote.api.routes :as api.routes]
   [ubinote.api.util :as api.u]
   [ubinote.config :as cfg]
   [ubinote.ui.page :as ui.page]))

;; TODO: should be a middleware
;; TODO#2: these redirect should be a real direct, not a rendering
(defn require-setup
  [handler]
  (fn [req]
    (if-not (cfg/setup?)
      ui.page/setup
      (handler req))))

(defn require-login
  [handler]
  (fn [req]
    (cond
     (not (cfg/setup?))
     (ui.page/setup req)

     (nil? api.u/*current-user-id*)
     (ui.page/login req)

     :else
     (handler req))))

(defroutes routes
  (GET "/health" _req "fine 😁")
  (context "/api" _req api.routes/routes)
  (GET "/build/bundle.js" _req (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" _req (resource-response "frontend/build/bundle.css"))
  (GET "/static/:file" [file] (resource-response (format "ui/static/%s" file)))

  ;; TODO these routings look weird
  (GET "/" _req (require-login ui.page/index))
  (GET "/comments" _req (require-login ui.page/comments-page))
  (GET "/page/:id" [id :<< as-int ] (require-login (partial ui.page/view-page id)))
  (GET "/public/page/:uuid" [uuid ] #(ui.page/view-page-public uuid %))
  (GET "/user" _req (require-login ui.page/user-page))
  (GET "/login" _req (require-setup ui.page/login))
  (GET "/setup" _req ui.page/setup)
  (route/not-found (ui.page/not-found)))
