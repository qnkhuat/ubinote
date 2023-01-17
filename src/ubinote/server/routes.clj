(ns ubinote.server.routes
  (:require
    [compojure.core :refer [context defroutes GET]]
    [ring.util.response :refer [resource-response]]
    [ubinote.api.core :as api]
    [ubinote.config :as cfg]))

(defroutes routes
  ;; serving bundle.js this seems hacky?
  (GET "/build/bundle.js" [_req] (resource-response "frontend/build/bundle.js"))
  (GET "/build/bundle.css" [_req] (resource-response "frontend/build/bundle.css"))
  (when cfg/is-dev?
    (GET "/build/bundle.js.map" [_req] (resource-response "frontend/build/bundle.js.map"))
    (GET "/build/bundle.css.map" [_req] (resource-response "frontend/build/bundle.css.map")))
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  ;; let svelte handles it from here
  (GET "*" [] (resource-response "frontend/index.html")))