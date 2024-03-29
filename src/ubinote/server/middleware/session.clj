(ns ubinote.server.middleware.session
  (:require
   [clojure.string :as str]
   [ring.util.response :as response]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]))

(defn https?
  "True if the original request made by the frontend client (i.e., browser) was made over HTTPS.

  In many production instances, a reverse proxy such as an ELB or nginx will handle SSL termination, and the actual
  request handled by Jetty will be over HTTP."
  [{{:strs [x-forwarded-proto x-forwarded-protocol x-url-scheme x-forwarded-ssl front-end-https origin]} :headers
    :keys                                                                                                [scheme]}]
  (cond
   ;; If `X-Forwarded-Proto` is present use that. There are several alternate headers that mean the same thing. See
   ;; https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto
   (or x-forwarded-proto x-forwarded-protocol x-url-scheme)
   (= "https" (str/lower-case (or x-forwarded-proto x-forwarded-protocol x-url-scheme)))

   ;; If none of those headers are present, look for presence of `X-Forwarded-Ssl` or `Frontend-End-Https`, which
   ;; will be set to `on` if the original request was over HTTPS.
   (or x-forwarded-ssl front-end-https)
   (= "on" (str/lower-case (or x-forwarded-ssl front-end-https)))

   ;; If none of the above are present, we are most not likely being accessed over a reverse proxy. Still, there's a
   ;; good chance `Origin` will be present because it should be sent with `POST` requests, and most auth requests are
   ;; `POST`. See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Origin
   origin
   (str/starts-with? (str/lower-case origin) "https")

   ;; Last but not least, if none of the above are set (meaning there are no proxy servers such as ELBs or nginx in
   ;; front of us), we can look directly at the scheme of the request sent to Jetty.
   scheme
   (= scheme :https)))

;; How do authenticated API requests work? Ubinote first looks for a cookie called `ubinote.SESSION`. This is the
;; normal way of doing things; this cookie gets set automatically upon login. `ubinote.SESSION` is an HttpOnly
;; cookie and thus can't be viewed by FE code.
;;
;; Finally we'll check for the presence of a `X-Ubinote-Session` header. If that isn't present, you don't have a
;; Session ID and thus are definitely not authenticated

(def ^String ubinote-session-cookie
  "The name of the cookie used to store the Ubinote session ID."
  "ubinote.SESSION")
(def ^:private ^String ubinote-session-header "x-ubinote-session")

(defn wrap-session-id
  [handler]
  (fn [{:keys [cookies headers] :as req}]
    (let [session-id (or (get-in cookies [ubinote-session-cookie :value])
                         (get headers ubinote-session-header))]
      (handler (assoc req :ubinote-session-id session-id)))))

(defn set-session-cookie
  [req response session]
  (let [is-https?      (https? req)
        cookie-options (merge
                        {:http-only true
                         :same-site :lax
                         :path      "/"}
                        ;; TODO: we will want to set a max age here
                        (when is-https?
                          {:secure true}))]
    (response/set-cookie response ubinote-session-cookie (str (:id session)) cookie-options)))

(defn- clear-cookie [response cookie-name]
  (response/set-cookie response cookie-name nil {:expires "Thu, 1 Jan 1970 00:00:00 GMT" :path "/"}))

(defn clear-session-cookie
  "Add a header to `response` to clear the current Ubinote session cookie."
  [response]
  (reduce clear-cookie response [ubinote-session-cookie]))

(defn- current-user-id-for-session
  "Return User ID and superuser status for Session with `session-id` if it is valid and not expired."
  [session-id]
  (when session-id
    (first (tc/query :default
                     :m/session
                     {:select [:id]
                      :from  [:core_user]
                      :where [:= :id {:select [:user_id]
                                      :from   [:session]
                                      ;; TODO: add expired time here
                                      :where  [:= :id session-id]}]}))))

(defn- find-user
  [id]
  (first (tc/query {:select [:*]
                    :from   [:core_user]
                    :where  [:= :id id]
                    :limit 1})))

(defmacro with-current-user
  [user-id & body]
  `(let [user-id# ~user-id]
     (binding [api.u/*current-user-id* user-id#
               api.u/*current-user*    (delay (find-user user-id#))]
       ~@body)))

(defn wrap-current-user-info
  "Add `:current-user-id` and `:current-user` to the request if a valid session token was passed."
  [handler]
  (fn [{:keys [ubinote-session-id] :as request}]
    (let [user-id (:id (current-user-id-for-session ubinote-session-id))]
      (with-current-user user-id
        (handler request)))))
