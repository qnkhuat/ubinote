(ns ubinote.models.user
  (:require [toucan.models :as models]
            [clojure.string :as str]))

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(defn pre-insert
  [{:keys [email] :as user}]
  (merge user
         {:email (str/lower-case email)}))

(models/defmodel User :core_user
  )


(extend (class User)
  models/IModel
  (merge models/IModelDefaults
         {:properties     (constantly {:timestamped? true})
          :default-fields (constantly default-user-columns)
          :hydration-keys (constantly [:creator])
          :pre-insert     pre-insert
          :types          (constantly {:coordinate :json})}))
