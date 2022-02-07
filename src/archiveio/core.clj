(ns archiveio.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [archiveio.api.core :as api]
            [compojure.route :as route]
            [compojure.core :refer [context defroutes GET]]))

(def ^:private middlewares
  ;; apply middleware from the first to last
  [wrap-keyword-params])

(defn- apply-middleware [handler]
  (reduce
    (fn [handler middleware-fn]
      (middleware-fn handler))
    handler
    middlewares))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" [] api/routes)
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (apply-middleware routes))

(defn -main []
  (run-jetty app {:port 3000
                  :join? false}))
