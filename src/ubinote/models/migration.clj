(ns ubinote.models.migration
  (:require [toucan.models :as models])
  (:import java.util.UUID))

(models/defmodel Migration :migration
  models/IModel
  (properties [_] {:created-at-timestamped? true}))
