(ns ubinote.models.user
  (:require
   [clojure.string :as str]
   [methodical.core :as m]
   [toucan2.core :as tc]
   [toucan2.tools.hydrate :as tc.hydrate]
   [ubinote.util.password :as passwd]))

;; --------------------------- Toucan methods  ---------------------------
(m/defmethod tc/table-name :m/user
  [_model]
  "core_user")

(derive :m/user :hooks/timestamped)

;; hydrations

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(tc/define-default-fields :m/user default-user-columns)

(m/defmethod tc.hydrate/model-for-automagic-hydration [:default :user]
  [_original-model _k]
  :m/user)

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:default :user :default]
  [_original-model _dest-key _hydrating-model]
  [:user_id])

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:m/page :user :default]
  [_original-model _dest-key _hydrating-model]
  [:creator_id])

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:m/comment :user :default]
  [_original-model _dest-key _hydrating-model]
  [:creator_id])

;; life cycles

(defn- hash-password
  [{:keys [password password_salt] :as user}]
  (when password_salt
    (throw (ex-info "password should not be encryped." {})))
  (let [salt (random-uuid)]
    (merge user
           {:password_salt salt
            :password      (passwd/hash-bcrypt (str salt password))})))

(tc/define-before-insert :m/user
  [{:keys [email] :as user}]
  (merge (hash-password user)
         {:email (str/lower-case email)}))

(tc/define-before-update :m/user
  [user]
  (let [{:keys [password]} (tc/changes user)]
    (merge user
           (when password
             (hash-password user)))))
