(ns ubinote.models.comment
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(models/defmodel Comment :comment)

(extend (class Comment)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})}))

(defn with-comments
  "Hydrate all comments for an annotaiton."
  {:hydrate :comments}
  [{annotation-id :id :as _annotation}]
  (db/select Comment :annotation_id annotation-id))
