(ns ubinote.api.page
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.controller.page :as page]
            [ubinote.api.common :as api]
            [ubinote.model.page :refer [Page]]
            [ubinote.model.annotation :refer [Annotation]]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [schema.core :as s]))

(def validate-add-page
  (s/validator page/NewPage))

(defn hydration
  [results]
  (hydrate results :user))

(defn add-page
  [{:keys [params] :as _req}]
  (validate-add-page params)
  ;; TODO, :user-id should take from session
  (page/create params))

(defn get-page
  [id _req]
  (let [page (-> (db/select-one Page :id id)
                 (hydrate :user :annotations
                          [:annotations :comments]))]
    (api/check-404 page)
    page))

(defn list-pages
  [_req]
  (-> (db/select Page)
      (hydrate :user)))

(defn get-annotation
  [id _req]
  (-> (db/select Annotation :page-id id)
      (hydrate :an)))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))))
