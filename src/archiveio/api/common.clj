(ns archiveio.api.common)

(def security-header
  {"X-Frame-Options" "DENY",
   "X-XSS-Protection" "1; mode=block",
   "Strict-Transport-Security" "max-age=31536000",
   "X-Permitted-Cross-Domain-Policies" "none",
   "Cache-Control" "max-age=0, no-cache, must-revalidate, proxy-revalidate",
   "X-Content-Type-Options" "nosniff"})

(defn check-400
  "Return Invalid Request if test failed"
  ([x]
   (check-400 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Invalid request." (merge {:status 400}
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
