(ns ubinote.server.middleware.core
  (:require [compojure.response :refer [Renderable]]
            [ubinote.server.middleware.paging :refer [wrap-paging]]
            [ubinote.server.middleware.exceptions :refer [wrap-api-exceptions]]
            [ubinote.server.middleware.session :refer [wrap-session-id wrap-current-user-info]]
            [ubinote.server.middleware.security :refer [add-security-header]]
            [ubinote.server.middleware.log :refer [wrap-request-logger]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

;; Enable endpoint to be able to just return an object or nil
;; it'll wrap the response in a proper ring's response
(defn- wrap-resp-if-needed
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

(defn wrap-json-body-kw
  [handler]
  (wrap-json-body handler {:keywords? true}))

(defn wrap-cors-un
  [handler]
  (wrap-cors handler
             :access-control-allow-origin #"http://localhost:8888/*"
             :access-control-allow-methods [:get :put :post :delete]))

(def middlewares
  ;; middleware will be applied from bottom->top
  ;; in the other words, the middleware at bottom will be executed last
  [wrap-cors-un             ;; TODO: this is temporarly, in production we don't need to enable CORS because our FE and BE are served from the the port
   wrap-cookies             ;; parses the cookies and assoc it to the request with :cookies key
   wrap-current-user-info
   wrap-session-id          ;; find the request session and assoc it to request with :ubinote-session-id key
   wrap-paging
   wrap-keyword-params      ;; normalizes string keys in :params to keyword keys
   wrap-json-body-kw        ;; parse the body of the request as map
   wrap-params              ;; parses GET and POST params as :query-params/:form-params and both as :params
   wrap-api-exceptions
   add-security-header      ;; add a set of security headers for all responses
   wrap-json-response       ;; normalize response to json
   wrap-request-logger])

(defn apply-middleware
  [handler middlewares]
  (reduce (fn [handler middleware]
            (middleware handler))
          handler
          middlewares))
