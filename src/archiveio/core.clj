(ns archiveio.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [context defroutes GET]]))

(def ^:private middlewares
  [])

(defn- apply-middleware [handler]
  (reduce
    (fn [handler middleware-fn]
      (middleware-fn handler))
    handler
    middlewares))

(defroutes routes
  (GET "/health" [_req] "fine 😁")
  (context "/api" []
           (GET "/health"
                [_req] "fine")))

(def app
  (apply-middleware routes))

(defn -main []
  (run-jetty app {:port 3000
                  :join? false}))
