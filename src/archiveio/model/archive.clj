(ns archiveio.model.archive
  (:require [toucan.models :as models]))

(models/defmodel Archive :archive
  models/IModel
  (properties [_] {:timestamped? true}))
