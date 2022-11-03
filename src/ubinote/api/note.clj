(ns ubinote.api.note
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models.note :refer [Note]]
            [schema.core :as s]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]))

(def NewNote
  {:annotation_id s/Int
   :creator_id    s/Int
   :content       s/Str})

(def ^:private validate-create-note
  "Schema for adding a user"
  (s/validator NewNote))

(defn create-note
  [{:keys [body current-user-id] :as _req}]
  (let [cmt (assoc body :creator_id current-user-id)]
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
