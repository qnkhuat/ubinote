(ns ubinote.api.page
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.model.page :refer [Page] :as page]
            [ubinote.model.common.schemas :as schemas]
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

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))))
