(ns ubinote.server.middleware.session
  (:require [ubinote.model.user :refer [User]]
            [ubinote.model.session :refer [Session]]
            [toucan.db :as db]))

;; How do authenticated API requests work? Ubinote first looks for a cookie called `ubinote.SESSION`. This is the
;; normal way of doing things; this cookie gets set automatically upon login. `ubinote.SESSION` is an HttpOnly
;; cookie and thus can't be viewed by FE code.
;;
;; Finally we'll check for the presence of a `X-Ubinote-Session` header. If that isn't present, you don't have a
;; Session ID and thus are definitely not authenticated

(def ^:private ^String ubinote-session-cookie "ubinote.SESSION")
(def ^:private ^String ubinote-session-header "x-ubinote-session")

(defn wrap-session-id
  [handler]
  (fn [{:keys [cookies headers] :as req}]
    (let [session-id (or (get-in cookies [ubinote-session-cookie :value])
                         (get headers ubinote-session-header))]
      (handler (assoc req :ubinote-session-id session-id)))))

;; TODO optimize this using raw sql because we run this on every request
;; TODO: we need to set cookie for the request
(defn- current-user-info-for-session
  "Return User ID and superuser status for Session with `session-id` if it is valid and not expired."
  [session-id]
  (when session-id
    (db/select-one User
                   {:where [:= :id (db/select-one-field :user_id Session
                                                        {:where [:= :id [:cast session-id :uuid]]
                                                         #_[:< :created_at]})]}))) ;; TODO: need to add expired time

(defn wrap-current-user-info
  "Add `:ubinote-user-id`, `:is-superuser?`, and :user-locale` to the request if a valid session token was passed."
  [handler]
  (fn [{:keys [ubinote-session-id] :as request}]
    (handler (assoc request :current-user (current-user-info-for-session ubinote-session-id)))))
