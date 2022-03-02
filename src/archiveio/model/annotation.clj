(ns archiveio.model.annotation
  (:require [toucan.models :as models]))

(models/defmodel Annotation :annotation
  models/IModel
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:annotation]))

