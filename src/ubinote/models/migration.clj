(ns ubinote.models.migration
  (:require [toucan.models :as models]))

(models/defmodel Migration :migration
  models/IModel
  (properties [_] {:timestamped? true}))
