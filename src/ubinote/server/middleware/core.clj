(ns ubinote.server.middleware.core
  (:require [ubinote.server.middleware.paging :refer [wrap-paging]]
            [ubinote.server.middleware.exceptions :refer [wrap-api-exceptions]]
            [ubinote.server.middleware.session :refer [wrap-session-id wrap-current-user-info]]
            [taoensso.timbre :as log]
            [ring.logger :as logger]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

(defn wrap-request-logger
  [handler]
  (logger/wrap-log-response handler {:log-fn (fn [{:keys [level throwable message]}]
                                              (log/log level throwable message))}))

(defn wrap-json-body-kw
  [handler]
  (wrap-json-body handler {:keywords? true}))

(defn wrap-cors-un
  [handler]
  (wrap-cors handler
             :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :put :post :delete]))

(def middlewares
  ;; middleware will be applied from bottom->top
  ;; in the other words, the middleware at bottom will be executed last
  [wrap-cors-un
   wrap-request-logger
   wrap-current-user-info
   wrap-session-id
   wrap-paging
   wrap-cookies
   wrap-keyword-params      ; normalizes string keys in :params to keyword keys
   wrap-json-body-kw        ; parse the body of the request as map
   wrap-params              ; parses GET and POST params as :query-params/:form-params and both as :params
   wrap-api-exceptions
   wrap-json-response])     ; normalize response to json form


(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))
