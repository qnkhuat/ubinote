(ns archiveio.model.annotation
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(models/defmodel Annotation :annotation
  models/IModel
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:annotation]))

(defn ^:hydrate annotations
  "Hydrate all annotaitons for an archive"
  [{archive-id :id :as _archive}]
  (db/select Annotation :archive-id archive-id))
