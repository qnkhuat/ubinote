(ns ubinote.model.page
  (:require [toucan.models :as models]))

(models/defmodel Page :page
  models/IModel
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:page]))
