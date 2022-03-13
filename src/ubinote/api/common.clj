(ns ubinote.api.common
  (:require [ring.util.response :refer [response?]]
            [compojure.response :refer [Renderable]]))

;; Enable endpoint to be able to just return an object or nil
;; it'll wrap the response in a proper ring's response
(defn- wrap-resp-if-needed
  [resp]
  (if (response? resp)
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
    (wrap-resp-if-needed (doall x))))

(def security-header
  {"X-Frame-Options" "DENY",
   "X-XSS-Protection" "1; mode=block",
   "Strict-Transport-Security" "max-age=31536000",
   "X-Permitted-Cross-Domain-Policies" "none",
   "Cache-Control" "max-age=0, no-cache, must-revalidate, proxy-revalidate",
   "X-Content-Type-Options" "nosniff"})

;; ---------------------------------------- check fns ----------------------------------------
(defn check-400
  "Return Invalid Request if test failed"
  ([x]
   (check-400 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Invalid request." (merge {:status 400}
                                               (when errors
                                                 {:errors errors})))))))

(defn check-401
  "Return Unauthorized if test failed"
  ([x]
   (check-401 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Unauthorized." (merge {:status 404}
                                         (when errors
                                           {:errors errors})))))))

(defn check-404
  "Return Not found if test failed"
  ([x]
   (check-404 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Not found." (merge {:status 404}
                                         (when errors
                                           {:errors errors})))))))
