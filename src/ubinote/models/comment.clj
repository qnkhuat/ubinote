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
                                  (group-by :annotation_id
                                            (tc/query :default :m/comment
                                                      {:select    [:c.* [:u.email :creator_email]]
                                                       :from      [[:comment :c]]
                                                       :left-join [[:core_user :u] [:= :c.creator_id :u.id]]
                                                       :where     [:in :c.annotation_id (map :id annotations)]
                                                       :order-by  [[:c.created_at :asc]]})))]
    (map #(assoc % :comments (get annotation-id->comments (:id %) [])) annotations)))


(m/defmethod tc.hydrate/batched-hydrate [:m/comment :comment_page]
  [_model _k comments]
  #p comments
  (def comments comments)
  comments)
