(ns ubinote.model.user
  (:require [toucan.models :as models]))

(def default-user-columns
  [:id :username :first-name :last-name :created-at :updated-at])

(models/defmodel User :core_user
  models/IModel
  (default-fields [_] default-user-columns)
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:user]))
