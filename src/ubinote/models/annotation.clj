(ns ubinote.models.annotation
  (:require
   [methodical.core :as m]
   [toucan2.core :as tc]
   [ubinote.models.interface :as mi]))

;; --------------------------- Toucan methods  ---------------------------

(m/defmethod tc/table-name :m/annotation
  [_model]
  "annotation")

(derive :m/annotation :hooks/timestamped)

(tc/deftransforms :m/annotation
  {:coordinate {:in  mi/json-in
                :out mi/json-out}})

;; life cycles

(tc/define-before-insert :m/annotation
  [annotation]
  (merge {:color "yellow"}
         annotation))

;; hydrations

(m/defmethod tc/batched-hydrate [:m/page :annotations]
  [_model _k pages]
  (when (seq pages)
    (let [annotations          (tc/select :m/annotation :page_id [:in (map :id pages)])
          page-id->annotations (group-by :page_id annotations)]
      (map #(assoc % :annotations (get page-id->annotations (:id %))) pages))))
