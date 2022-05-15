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
   (s/optional-key :tags) [s/Str]})

(def validate-add-page
  (s/validator NewPage))

(defn- add-page
  [{:keys [params current-user] :as _req}]
  (validate-add-page params)
  (page/create-page (assoc params :creator_id (:id current-user))))

(defn- get-page
  [id _req]
  (api/check-404 (-> (db/select-one Page :id id)
                     (hydrate :user :annotations
                              [:annotations :comments]))))

(defn- list-pages
  [_req]
  (-> (db/select Page)
      (hydrate :user)))

(def NewAnnotation
  {:coordinate             {:start s/Num
                            :end   s/Num}
   (s/optional-key :color) (s/maybe s/Str)})

(def ^:private validate-create-annotation
  (s/validator NewAnnotation))

(defn- add-annotation
  [id {:keys [body current-user-id] :as _req}]
  (validate-create-annotation body)
  (db/insert! Annotation
              (assoc body
                     :page_id id
                     :creator_id current-user-id)))

(defn- get-annotation
  [id {:keys [params] :as _req}]
  (validate-create-annotation params)
  (db/select Annotation :page_id id))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           ;; Get all annotations for a page
           (GET "/annotation" [] (partial get-annotation id))
           ;; Create annotation for a page
           (POST "/annotation" [] (partial add-annotation id))))
