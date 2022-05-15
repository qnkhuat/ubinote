(ns ubinote.models.common
  (:require [cheshire.core :as json]
            [toucan.models :as models]))

(defn- add-created-at-timestamp [obj & _]
  (assoc obj :created_at :%now))

(defn- add-updated-at-timestamp [obj & _]
  (assoc obj :updated_at :%now))

(models/add-property! :timestamped?
                      :insert (comp add-created-at-timestamp add-updated-at-timestamp)
                      :update add-updated-at-timestamp)

;; like `timestamped?`, but for models that only have an `:updated_at` column
(models/add-property! :updated-at-timestamped?
                      :insert add-updated-at-timestamp
                      :update add-updated-at-timestamp)

;; like `timestamped?`, but for models that only have an `:updated_at` column
(models/add-property! :created-at-timestamped?
                      :insert add-created-at-timestamp
                      :update add-created-at-timestamp)


(models/add-type! :json
                  :in  json/generate-string
                  :out #(json/parse-string % keyword))
