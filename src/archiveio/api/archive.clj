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
  (let [archive #p (-> (db/select-one Archive :id id)
                    (hydrate :annotation))]
    (resp/assert-404 archive "Archive not found")
    (resp/entity-response 200 archive)))

;(hydrate (db/select-one Archive :id 1) :user)

;(hydrate (db/select-one Annotation :id 1) :archive)
;
;(hydrate (Annotation 1) :user)
;;; => {:id 1,
; :user-id 1,
; :archive-id 1,
; :color "red",
; :coordinate "sth",
; :created-at #inst "2022-02-28T17:29:19.608288000-00:00",
; :updated-at #inst "2022-02-28T17:29:19.608288000-00:00"}

;
;
;(hydrate (db/select-one Annotation :id 1) :archive)

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/" [] list-archives)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-archive id))))
