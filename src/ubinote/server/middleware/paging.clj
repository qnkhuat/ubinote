(ns ubinote.server.middleware.paging)

(def default-offset 0)
(def default-limit 50)
(def ^:dynamic *limit* nil)
(def ^:dynamic *offset* nil)

(defn parse-paging-params
  [{:keys [limit offset] :as _params}]
  (let [limit  (min (or (some-> limit Integer/parseUnsignedInt)
                        default-limit)
                    300)
        offset (or (some-> offset Integer/parseUnsignedInt)
                   default-offset)]
    {:limit  limit
     :offset offset
     :paged? true}))

(defn paged?
  [{:keys [limit offset] :as _params}]
  (or limit offset))

(defn wrap-paging
  "Check if a request needs to paging or not
  If it's then this will automatically add [:limit :paging] keys to request's params"
  [handler]
  (fn [{:keys [params] :as request}]
    (if (paged? params)
      (let [paging-params (try
                            (parse-paging-params params)
                            (catch Throwable e
                              e))]
        (if (instance? Throwable paging-params)
          (throw (ex-info "Failed to parse paging params" {:status-code 400
                                                           :body        {:message (ex-message paging-params)}}))
          (handler (assoc request :params (merge params paging-params)))))
      (handler request))))
