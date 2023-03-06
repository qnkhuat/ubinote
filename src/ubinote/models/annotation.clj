(ns ubinote.models.annotation
  (:require
    [methodical.core :as m]
    [toucan.db :as db]
    [toucan.models :as models]
    [toucan2.core :as tc]
    [toucan2.tools.hydrate :as tc.hydrate]))

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

(defn- pre-insert
  [annotation]
  ;; default color to red
  (merge {:color "red"}
         annotation))

(tc/define-before-insert :m/annotation
  [annotation]
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
