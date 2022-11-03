(ns ubinote.models.note
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(models/defmodel Note :note)

(extend (class Note)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})}))

(defn with-notes
  "Hydrate all notes for an annotaiton."
  {:hydrate :notes}
  [{annotation-id :id :as _annotation}]
  (db/select Note :annotation_id annotation-id))
