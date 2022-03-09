(ns ubinote.server.middleware.core
  (:require [ubinote.server.middleware.paging :refer [wrap-paging]]
            [ubinote.server.middleware.api :refer [wrap-response-if-needed wrap-api-exceptions]]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [ring.logger :as logger]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]))

(defn wrap-json-params-normalize
  "wrap-json-parms but with underscores->dashes"
  [handler]
  (wrap-json-params handler {:key-fn (fn [k]
                                       (string/replace k "_" "-"))}))

(defn wrap-json-response-normalize
  "wrap-json-response but with normalize dashes->underscores"
  [handler]
  (wrap-json-response handler {:key-fn (fn [k]
                                         (string/replace (name k) "-" "_"))}))

(defn wrap-request-logger
  [handler]
  (logger/wrap-with-logger handler {:log-fn (fn [{:keys [level throwable message]}]
                                              (log/log level throwable message))}))

(defn wrap-cors-un
  [handler]
  (wrap-cors handler
             :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :put :post :delete]))

(def middlewares
  ;; middleware will be applied from bottom->top
  [
   wrap-api-exceptions
   wrap-response-if-needed      ; if resp is an object, turn it into a response
   wrap-request-logger
   wrap-json-response-normalize ; normalize response to json form
   wrap-json-response
   wrap-paging
   wrap-keyword-params          ; normalizes string keys in :params to keyword keys
   wrap-json-params-normalize   ; extracts json POST body and makes it avaliable on request
   wrap-params                  ; parses GET and POST params as :query-params/:form-params and both as :params
   wrap-cors-un
   ])

(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))
