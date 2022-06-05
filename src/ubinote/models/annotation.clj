(ns ubinote.models.annotation
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(defn- pre-insert
  [annotation]
  ;; default color to red
  (merge {:color "red"}
         annotation))

(models/defmodel Annotation :annotation)

(extend (class Annotation)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})
          :pre-insert pre-insert
          :types      (constantly {:coordinate :json})}))

(defn with-annotations
  "Hydrate all annotaitons for a page."
  {:hydrate :annotations}
  [{page_id :id :as _page}]
  (db/select Annotation :page_id page_id))
