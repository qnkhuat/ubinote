(ns ubinote.models.annotation
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(defn- pre-insert
  [annotation]
  (merge {:color "red"}
         annotation))

(models/defmodel Annotation :annotation)

(defn hydrate-annotations
  "Hydrate all annotaitons for a page."
  {:hydrate :annotations}
  [{page_id :id :as _page}]
  (db/select Annotation :page_id page_id))

(extend (class Annotation)             ; it's somewhat more readable to write `(class User)` instead of `UserInstance`
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})
          :pre-insert pre-insert
          :types      (constantly {:coordinate :json})}))

