(ns ubinote.model.session
  (:require [toucan.models :as models]))

(models/defmodel Session :session
  models/IModel
  (properties [_] {:created-at-timestamped? true})
  (hydration-keys [_] [:session]))
