(ns archiveio.model.comment
  (:require [toucan.models :as models]))

(models/defmodel Comment :comment
  models/IModel
  (properties [_] {:timestamped? true}))
