(ns ubinote.api.page
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.models :refer [Page Annotation]]
            [ubinote.models.page :as page]
            [ubinote.models.common.schemas :as schemas]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [schema.core :as s]))

(s/def NewPage
  {:url                   schemas/URL
   :creator_id            s/Int
   (s/optional-key :tags) [s/Str]})

(def validate-add-page
  (s/validator NewPage))

(defn- add-page
  [{:keys [body current-user] :as _req}]
  (-> (assoc body :creator_id (:id current-user))
      validate-add-page
      page/create-page))

(defn- get-page
  [id _req]
  (-> (api/check-404 (db/select-one Page :id id))
      (hydrate :user :annotations
               [:annotations :comments])))

(defn- list-pages
  [_req]
  (-> (db/select Page)
      (hydrate :user)))

(defn- get-annotation
  [id _req]
  (db/select Annotation :page_id id))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           ;; Get all annotations for a page
           (GET "/annotation" [] (partial get-annotation id))))
