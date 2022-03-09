(ns ubinote.model.common
  (:require [toucan.models :as models]))

(defn- add-created-at-timestamp [obj & _]
  (assoc obj :created-at :%now))

(defn- add-updated-at-timestamp [obj & _]
  (assoc obj :updated-at :%now))

(models/add-property! :timestamped?
                      :insert (comp add-created-at-timestamp add-updated-at-timestamp)
                      :update add-updated-at-timestamp)

;; like `timestamped?`, but for models that only have an `:updated_at` column
(models/add-property! :updated-at-timestamped?
                      :insert add-updated-at-timestamp
                      :update add-updated-at-timestamp)
