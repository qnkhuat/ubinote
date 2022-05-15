(ns ubinote.model.user
  (:require [toucan.models :as models]
            [clojure.string :as str]))

(def default-user-columns
  [:id :email :first_name :last_name :created_at :updated_at])

(defn pre-insert-hook
  [{:keys [email] :as user}]
  (merge user
         {:email (str/lower-case email)}))

(models/defmodel User :core_user
  models/IModel
  (default-fields [_] default-user-columns)
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:user])
  (pre-insert [user] (pre-insert-hook user)))
