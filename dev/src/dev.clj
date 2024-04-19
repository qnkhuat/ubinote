(ns dev
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [hashp.core :as hashp]
   [ring.util.codec :as codec]
   [toucan2.core :as tc]
   [ubinote.server :as server]
   [ubinote.server.middleware.session :as mw.session]
   [ubinote.ui.template :as template]))

(defonce ^:private instance* (atom nil))

(defn start! []
  (reset! instance* (server/start! #'server/app)))

(defn stop! []
  (when @instance*
    (.stop @instance*)
    (reset! instance* nil)))

(defn restart! []
  (stop!)
  (start!)
  nil)

(defn- build-query-string
  [query-parameters]
  (str/join \& (letfn [(url-encode [s]
                         (cond-> s
                           (keyword? s) name
                           (some? s)    codec/url-encode))
                       (encode-key-value [k v]
                         (str (url-encode k) \= (url-encode v)))]
                 (flatten (for [[k value-or-values] query-parameters]
                            (if (sequential? value-or-values)
                              (for [v value-or-values]
                                (encode-key-value k v))
                              [(encode-key-value k value-or-values)]))))))

(defn- build-mock-request
  [{:keys [query url body method session-id]}]
  (let [url             (cond->> (first (str/split url #"\?")) ;; strip out the query param parts if any
                          (not= (first url) \/)
                          (str "/"))]
    (-> (merge
         {:accept         "json"
          :headers        {"content-type" "application/json"
                           @#'mw.session/ubinote-session-header session-id}
          :query-string   (build-query-string query)
          :request-method method
          :uri            url}
         (when (seq body)
           {:body (java.io.ByteArrayInputStream.
                   (.getBytes (if (string? body)
                                ^String body
                                (json/generate-string body))))})))))

(defn user-http-request
 ([method url]
  (user-http-request (tc/select-one-pk :m/user) method url {} nil))
 ([user-id method url]
  (user-http-request user-id method url {} nil))
 ([user-id method url query]
  (user-http-request user-id method url query nil))
 ([user-id method url query body]
  (let [session-id (or (tc/select-one-pk :m/session :user_id user-id)
                       (tc/insert-returning-pk! :m/session {:user_id user-id}))]
    (binding [template/*return-raw-hiccup* true]
      (let [resp (-> (build-mock-request {:url        url
                                          :method     method
                                          :query      query
                                          :body       body
                                          :session-id session-id})
                     server/app)
            body (:body resp)]
        (case (get-in resp [:headers "Content-Type"])
          "text/html" body ;; this should be hiccup
          (json/parse-string keyword body)))))))

(defmacro p
  "#p, but to use in pipelines like `(-> 1 inc dev/p inc)`.

  See https://github.com/weavejester/hashp"
  [form]
  (hashp/p* form))
