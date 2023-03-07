(ns ubinote.models.annotation
  (:require
    [methodical.core :as m]
    [toucan2.core :as tc]
    [toucan2.tools.hydrate :as tc.hydrate]
    [ubinote.models.interface :as mi]))

;; --------------------------- Toucan methods  ---------------------------

(m/defmethod tc/table-name :m/annotation
  [_model]
  "annotation")

(m/defmethod tc.hydrate/model-for-automagic-hydration [:default :annotation]
  [_original-model _k]
  :m/annotation)

(m/defmethod tc.hydrate/fk-keys-for-automagic-hydration [:default :annotation :m/annotation]
  [_original-model _dest-key _hydrating-model]
  [:annotation_id])

(tc/deftransforms :m/annotation
 {:coordinate {:in  mi/json-in
               :out mi/json-out}})

(m/defmethod tc.hydrate/simple-hydrate [:m/page :annotations]
  [_model _k instance]
  (assoc instance :annotations (tc/select :m/annotation :page_id (:id instance))))

(tc/define-before-insert :m/annotation
  [annotation]
  (merge {:color "red"}
         annotation))

(derive :m/annotation :hooks/timestamped)
