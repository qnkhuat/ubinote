(ns ubinote.models.comment
  (:require
   [methodical.core :as m]
   [toucan2.core :as tc]))

(m/defmethod tc/table-name :m/comment
  [_model]
  "comment")

(derive :m/comment :hooks/timestamped)

;; hydrations

(m/defmethod tc/batched-hydrate [:m/annotation :comments]
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


(m/defmethod tc/batched-hydrate [:m/comment :comment-info]
  [_model _k comments]
  (when (seq comments)
    (let [annotation-id->page (as-> (tc/query :default :m/page
                                              {:select    [[:a.id :annotation_id] :p.*]
                                               :from      [[:page :p]]
                                               :left-join [[:annotation :a] [:= :p.id :a.page_id]]
                                               :where     [:in :a.id (map :annotation_id (tc/select :m/comment ))]}) rows
                                (group-by :annotation_id rows)
                                (update-vals rows first))
          user-id->user (tc/select-pk->fn identity :m/user :id [:in (map :creator_id comments)])]
      (map (fn [{:keys [creator_id annotation_id] :as cmt}]
             (assoc cmt
                    :creator (get user-id->user creator_id)
                    :page     (get annotation-id->page annotation_id)))
           comments))))
