(ns ubinote.ui.trigger
  (:require
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.models.annotation :as annotation]))

(defn update-annotation-color-trigger
  [annotation-id new-class]
  {:trigger-update-annotation-color {:annotation-id annotation-id
                                     :new-class     new-class}})

(defn update-annotation-color-if-needed
  [annotation-id resp]
  (let [cmt-count (tc/count :m/comment :annotation_id annotation-id)]
    (cond-> resp
      ;; removed all comments
      (zero? cmt-count)
      (api.u/htmx-trigger (update-annotation-color-trigger annotation-id (annotation/annotation-color-class false)))

      ;; just added a new comment
      (= 1 cmt-count)
      (api.u/htmx-trigger (update-annotation-color-trigger annotation-id (annotation/annotation-color-class true))))))
