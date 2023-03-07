(ns ubinote.models.comment
  (:require
    [methodical.core :as m]
    [toucan2.core :as tc]
    [toucan2.tools.hydrate :as tc.hydrate]))

(m/defmethod tc/table-name :m/comment
  [_model]
  "comment")

(derive :m/comment :hooks/timestamped)

;; hydrations

(m/defmethod tc.hydrate/batched-hydrate [:m/annotation :comments]
  [_model _k annotations]
  (let [annotation-id->comments (when (seq annotations)
                                  (->> (tc/select :m/comment :annotation_id [:in (map :id annotations)])
                                       (group-by :annotation_id)))]
    (map #(assoc % :comments (get annotation-id->comments (:id %) [])) annotations)))
