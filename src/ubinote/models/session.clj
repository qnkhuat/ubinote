(ns ubinote.models.session
  (:require [toucan.models :as models]))

(models/defmodel Session :session)

(extend (class Session)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:created-at-timestamped? true})}))
