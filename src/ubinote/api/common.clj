(ns ubinote.api.common)

;; ---------------------------------------- check fns ----------------------------------------
(defn check-400
  "Return Invalid Request if test failed."
  ([x]
   (check-400 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Invalid request." (merge {:status-code 400}
                                               (when errors
                                                 {:errors errors})))))))

(defn check-401
  "Return Unauthorized if test failed."
  ([x]
   (check-401 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Unauthorized." (merge {:status-code 401}
                                            (when errors
                                              {:errors errors})))))
   x))

(defn check-404
  "Return Not found if test failed."
  ([x]
   (check-404 x nil))
  ([x errors]
   (when-not x
     (throw (ex-info "Not found." (merge {:status-code 404}
                                         (when errors
                                           {:errors errors})))))
   x))
