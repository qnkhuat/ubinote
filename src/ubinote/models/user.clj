(ns ubinote.models.user
  (:require
   [clojure.string :as str]
   [methodical.core :as m]
   [toucan.models :as models]
   [toucan2.core :as tc]
   [toucan2.tools.hydrate :as tc.hydrate]
   [ubinote.util.password :as passwd]))

;; --------------------------- Toucan methods  ---------------------------
(m/defmethod tc/table-name :m/user
  [_model]
  "core_user")

(m/defmethod tc.hydrate/model-for-automagic-hydration [:default :user]
  [_original-model _k]
  :m/user)

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:default :user :default]
  [_original-model _dest-key _hydrating-model]
  [:user_id])

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:m/page :user :default]
  [_original-model _dest-key _hydrating-model]
  [:creator_id])

(models/defmodel User :core_user)

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(defn- hash-password
  [{:keys [password password_salt] :as user}]
  (when password_salt
    (throw (ex-info "password should not be encryped." {})))
  (let [salt (random-uuid)]
    (merge user
           {:password_salt salt
            :password      (passwd/hash-bcrypt (str salt password))})))

(defn- pre-insert
  [{:keys [email] :as user}]
  (merge (hash-password user)
         {:email (str/lower-case email)}))


(defn- pre-update
  [{:keys [password] :as user}]
  (merge user
         (when password
           (hash-password user))))

(extend (class User)
  models/IModel
  (merge models/IModelDefaults
         {:properties     (constantly {:timestamped? true})
          :default-fields (constantly default-user-columns)
          ;; any table that has creator_id column can hydrate user
          :hydration-keys (constantly [:creator])
          :pre-insert     pre-insert
          :pre-update     pre-update
          :types          (constantly {:coordinate :json})}))

(tc/define-before-insert :m/user
  [{:keys [email] :as user}]
  (pre-insert user))

(tc/define-before-update :m/user
  [{:keys [password] :as user}]
  (pre-update user))
