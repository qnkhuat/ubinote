(ns ubinote.server.routes
  (:require
    [compojure.core :refer [context defroutes GET]]
    [ring.util.response :refer [resource-response]]
    [ubinote.api :as api]
    [ubinote.config :as cfg]))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (GET "/build/bundle.js" [_req] (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" [_req] (resource-response "frontend/build/bundle.css"))
  #_(GET "/build/bundle.js.map" [_req] (resource-response "frontend/build/bundle.js.map"))
  #_(GET "/build/bundle.css.map" [_req] (resource-response "frontend/build/bundle.css.map"))
  ;; let svelte handles it from here
  (GET "*" [] (resource-response "frontend/index.html")))
