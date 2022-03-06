(ns archiveio.api.archive
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.controller.archive :as archive]
            [archiveio.api.response :as resp]
            [archiveio.model.archive :refer [Archive]]
            [archiveio.model.annotation :refer [Annotation]]
            [archiveio.model.comment :refer [Comment]]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [schema.core :as s]))

(def validate-add-archive
  (s/validator archive/NewArchive))

(defn add-archive
  [{:keys [params] :as _req}]
  (validate-add-archive params)
  ;; TODO, :user-id should take from session
  (resp/entity-response 200 (archive/create params)))

(defn get-archive
  [id _req]
  (let [archive (-> (db/select-one Archive :id id)
                    (hydrate :annotation))]
    (resp/assert-404 archive "Archive not found")
    (resp/entity-response 200 archive)))

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/" [] list-archives)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-archive id))))
