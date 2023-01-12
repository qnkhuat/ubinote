(ns ubinote.api.note
  (:require [compojure.coercions :refer [as-int]]
            [compojure.core :refer [context defroutes POST GET]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [ubinote.api.common :as api]
            [ubinote.models.note :refer [Note]]))

(def NewNote
  {:annotation_id s/Int
   :creator_id    s/Int
   :content       s/Str})

(def ^:private validate-create-note
  "Schema for adding a note"
  (s/validator NewNote))

(defn create-note
  [{:keys [body] :as _req}]
  (let [cmt (assoc body :creator_id api/*current-user-id*)]
    (validate-create-note cmt)
    (db/insert! Note cmt)))

(defn get-note
  [id _req]
  (-> (db/select-one Note :id id)
      (hydrate :annotation :user)
      api/check-404))

(defroutes routes
  (POST "/" [] create-note)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-note id))))
