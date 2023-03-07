(ns ubinote.models.interface
  (:require
   [cheshire.core :as json]
   [toucan2.core :as tc]))

(def json-in json/generate-string)

(def json-out (fn [x] (json/parse-string x keyword)))

(defn- add-created-at-timestamp [obj & _]
  (assoc obj :created_at :%now))

(defn- add-updated-at-timestamp [obj & _]
  (assoc obj :updated_at :%now))

(tc/define-before-insert :hooks/timestamped
  [instance]
  (-> instance
      add-created-at-timestamp
      add-updated-at-timestamp))

(tc/define-before-update :hooks/timestamped
  [instance]
  (-> instance
      add-updated-at-timestamp))

(tc/define-before-insert :hooks/updated-at-timestamped
  [instance]
  (-> instance
      add-updated-at-timestamp))

(tc/define-before-update :hooks/updated-at-timestamped
  [instance]
  (-> instance
      add-updated-at-timestamp))

(tc/define-before-insert :hooks/created-at-timestamped
  [instance]
  (-> instance
      add-created-at-timestamp))
