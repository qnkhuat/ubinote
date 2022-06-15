(ns ubinote.server.middleware.security)

(def security-header
  {"X-Frame-Options" "DENY",
   "X-XSS-Protection" "1; mode=block",
   "Strict-Transport-Security" "max-age=31536000",
   "X-Permitted-Cross-Domain-Policies" "none",
   "Cache-Control" "max-age=0, no-cache, must-revalidate, proxy-revalidate",})

(defn add-security-header
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (update resp :headers merge security-header))))

