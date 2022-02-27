(ns archiveio.model.user
  (:require [toucan.models :as models]))

(def ^:private default-user-columns
  [:id :email :first-name :last-name :created-at :updated-at])

(models/defmodel User :core_user
  models/IModel
  (default-fields [_] default-user-columns)
  (properties [_] {:timestamped? true}))
