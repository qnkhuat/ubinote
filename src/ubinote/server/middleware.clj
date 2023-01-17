(ns ubinote.server.middleware
  (:require
    [compojure.response :refer [Renderable]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ubinote.config :as cfg]
    [ubinote.server.middleware.exceptions :refer [wrap-api-exceptions]]
    [ubinote.server.middleware.log :refer [wrap-request-logger]]
    [ubinote.server.middleware.paging :refer [wrap-paging]]
    [ubinote.server.middleware.security :refer [add-security-header]]
    [ubinote.server.middleware.session :refer [wrap-session-id wrap-current-user-info]]))

(defn- wrap-resp-if-needed
  " Enable endpoint to be able to just return an object or nil
  it'll wrap the response in a proper ring's response"
  [resp]
  (if (and (:status resp) (contains? resp :body))
    resp
    {:status 200
     :body   resp}))

(extend-protocol Renderable
  nil
  (render [_ _]
    {:status 204 :body nil})

  clojure.lang.IPersistentMap
  (render [x _request]
    (wrap-resp-if-needed x))

  clojure.lang.PersistentVector
  (render [x _request]
    (wrap-resp-if-needed x))

  clojure.lang.LazySeq
  (render [x _request]
    ;; realize the seq and return it
    (wrap-resp-if-needed (doall x))))

(defn- wrap-json-body-kw
  [handler]
  (wrap-json-body handler {:keywords? true}))

(defn- ubinote-wrap-cors-in-dev
  [handler]
  ;; this is for dev mode only and it allows We make call from hot-reloading
  ;; host on FE
  (if (= :dev cfg/run-mode)
    (wrap-cors handler
               :access-control-allow-credentials "true"
               :access-control-allow-origin      #"http://localhost:8080/*"
               :access-control-allow-methods     [:options :get :put :post :delete])
    handler))

(def middlewares
  ;; middleware will be applied from bottom->top
  ;; in the other words, the middleware at bottom will be executed last
  ;; ▼▼▼ POST-PROCESSING ▼▼▼ happens from TOP-TO-BOTTOM
  [ubinote-wrap-cors-in-dev
   wrap-api-exceptions
   wrap-request-logger
   wrap-current-user-info
   wrap-session-id          ;; find the request session and assoc it to request with :ubinote-session-id key
   wrap-cookies             ;; parses the cookies and assoc it to the request with :cookies key
   wrap-paging
   wrap-keyword-params      ;; normalizes string keys in :params to keyword keys
   wrap-json-body-kw        ;; parse the body of the request as map
   wrap-params              ;; parses GET and POST params as :query-params/:form-params and both as :params
   add-security-header      ;; add a set of security headers for all responses
   wrap-json-response])     ;; normalize response to json
;; ▲▲▲ PRE-PROCESSING ▲▲▲ happens from BOTTOM-TO-TOP


(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))
