(ns ubinote.models.comment
  (:require [toucan.models :as models]))

(models/defmodel Comment :comment)

(extend (class Comment)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})}))
