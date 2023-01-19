(ns ubinote.models
  (:require
    [cheshire.core :as json]
    [potemkin :as p]
    [toucan.models :as models]
    [ubinote.models.annotation :as annotation]
    [ubinote.models.note :as m-note]
    [ubinote.models.page :as page]
    [ubinote.models.user :as user]))

(comment
  page/keep-me
  user/keep-me
  annotation/keep-me
  m-note/keep-me)

(p/import-vars
  [page Page]
  [user User]
  [annotation Annotation]
  [m-note Note])

;; --------------------------- Adding toucan properties and types ------------------------
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

;; like `timestamped?`, but for models that only have an `:created_at` column
(models/add-property! :created-at-timestamped?
                      :insert add-created-at-timestamp
                      :update add-created-at-timestamp)

(models/add-type! :json
                  :in  json/generate-string
                  :out #(json/parse-string % keyword))
