(ns archiveio.model.user
  (:require [toucan.models :as models]))

(def ^:private default-user-columns
  [:email :first-name :last-name :created-at :updated-at])

(models/defmodel User :core_user
  models/IModel
  (defaul-fields [_] default-user-columns))
