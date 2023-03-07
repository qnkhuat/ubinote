(ns ubinote.models.migration
  (:require
    [methodical.core :as m]
    [toucan2.core :as tc]))

(m/defmethod tc/table-name :m/migration
  [_model]
  "migration")

(derive :m/migration :hooks/created-at-timestamped)
