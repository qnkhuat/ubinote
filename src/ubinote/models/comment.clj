(ns ubinote.models.comment
  (:require
    [methodical.core :as m]
    [toucan.db :as db]
    [toucan.models :as models]
    [toucan2.core :as tc]))

(models/defmodel Comment :comment)

(m/defmethod tc/table-name :m/comment
  [_model]
  "comment")

(extend (class Comment)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})}))

(defn with-comments
  "Hydrate all notes for an annotation"
  {:hydrate :comments}
  [{annotation-id :id :as _annotation}]
  (db/select Comment :annotation_id annotation-id))
