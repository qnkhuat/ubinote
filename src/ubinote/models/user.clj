(ns ubinote.models.user
  (:require
   [clojure.string :as str]
   [toucan.models :as models]
   [ubinote.util.password :as passwd]))

(models/defmodel User :core_user)

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(defn- hash-password
  [{:keys [password] :as user}]
  (if password
    (assoc user :password (passwd/hash-bcrypt password))
    user))

(defn- pre-insert
  [{:keys [email password] :as user}]
  (merge user
         {:email (str/lower-case email)}
         (when password
           {:password (passwd/hash-bcrypt password)})))

(defn- pre-update
  [{:keys [password] :as user}]
  (merge user
         (when password
           {:password (passwd/hash-bcrypt password)})))

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
