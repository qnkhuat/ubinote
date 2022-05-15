(ns ubinote.models.annotation
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(defn- pre-insert-hook
  [annotation]
  (merge {:color "red"}
         annotation))

(models/defmodel Annotation :annotation
  models/IModel
  (properties     [_] {:timestamped? true})
  (types          [_] {:coordinate :json})
  (hydration-keys [_] [:annotation])
  (pre-insert     [annotation] (pre-insert-hook annotation)))

(defn hydrate-annotations
  "Hydrate all annotaitons for a page."
  {:hydrate :annotations}
  [{page_id :id :as _page}]
  (db/select Annotation :page_id page_id))
