(ns ubinote.api.comment
  (:require [compojure.coercions :refer [as-int]]
            [compojure.core :refer [context defroutes POST GET]]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [ubinote.api.common :as api]
            [ubinote.models :refer [Comment]]
            [ubinote.models.common.schema :as schema]))

(def NewNote
  [:map
   [:annotation_id schema/IntegerGreaterThanZero]
   [:creator_id schema/IntegerGreaterThanZero]
   [:content schema/NonBlankString]])

(defn create-comment
  [{:keys [body] :as _req}]
  (let [cmt (assoc body :creator_id api/*current-user-id*)]
    (schema/validate-schema cmt NewNote)
    (db/insert! Comment cmt)))

(defn get-comment
  [id _req]
  (-> (db/select-one Comment :id id)
      (hydrate :annotation :user)
      api/check-404))

(defroutes routes
  (POST "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-comment id))))
