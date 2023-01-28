(ns ubinote.api.page
  (:require [compojure.coercions :refer [as-int]]
            [compojure.core :refer [context defroutes POST GET]]
            [malli.core :as mc]
            [ring.util.response :as response]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
            [ubinote.api.common :as api]
            [ubinote.models :refer [Page]]
            [ubinote.models.common.schema :as schema]
            [ubinote.models.page :as page]))

(def NewPage
  (mc/schema
    [:map
     [:url schema/URL]
     [:creator_id schema/IntegerGreaterThanZero]
     [:tags       {:optional true} [:sequential :string]]]))

(defn- add-page
  [{:keys [body] :as _req}]
  (-> (assoc body :creator_id api/*current-user-id*)
      (schema/validate-schema NewPage)
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

(defn- get-page-content
  "Returns the static file of the page"
  [id _req]
  (-> (api/check-404 (db/select-one-field :path Page :id id))
      (response/file-response {:root page/root})
      (response/content-type "text/html")))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           (GET "/content" [] (partial get-page-content id))))
