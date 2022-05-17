(ns ubinote.models.annotation
  (:require [ubinote.models.comment :refer [Comment]]
            [toucan.db :as db]
            [toucan.models :as models]))

(defn- pre-insert
  [annotation]
  ;; default color to red
  (merge {:color "red"}
         annotation))

(models/defmodel Annotation :annotation)

(extend (class Annotation)             ; it's somewhat more readable to write `(class User)` instead of `UserInstance`
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})
          :pre-insert pre-insert
          :types      (constantly {:coordinate :json})}))

(defn with-comments
  "Hydrate all comments for an annotaiton."
  {:hydrate :comments}
  [{annotation-id :id :as _annotation}]
  (db/select Comment :annotation_id annotation-id))
