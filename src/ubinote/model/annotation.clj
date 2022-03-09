(ns ubinote.model.annotation
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(models/defmodel Annotation :annotation
  models/IModel
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:annotation]))

(defn hydrate-annotations
  "Hydrate all annotaitons for an page"
  {:hydrate :annotations}
  [{page-id :id :as _page}]
  (db/select Annotation :page-id page-id))
