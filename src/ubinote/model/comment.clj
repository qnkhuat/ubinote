(ns ubinote.model.comment
  (:require [toucan.db :as db]
            [toucan.models :as models]))

(models/defmodel Comment :comment
  models/IModel
  (properties [_] {:timestamped? true})
  (hydration-keys [_] [:comment]))

(defn hydrate-comments
  "Hydrate all comments for an annotaiton"
  {:hydrate :comments}
  [{annotation-id :id :as _comment}]
  (db/select Comment :annotation-id annotation-id))
