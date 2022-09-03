(ns ubinote.models.user
  (:require [toucan.models :as models]
            [cemerick.friend.credentials :as creds]
            [clojure.string :as str]))

(models/defmodel User :core_user)

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(defn- hash-password
  [{:keys [password] :as user}]
  (if password
    (assoc user :password (creds/hash-bcrypt password))
    user))

(defn- pre-insert
  [{:keys [email password] :as user}]
  (merge user
         {:email (str/lower-case email)}
         (when password
           {:password (creds/hash-bcrypt password)})))

(defn- pre-update
  [{:keys [password] :as user}]
  (merge user
         (when password
           {:password (creds/hash-bcrypt password)})))

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
