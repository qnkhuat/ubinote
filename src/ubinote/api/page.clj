(ns ubinote.api.page
  (:require
    [compojure.coercions :refer [as-int]]
    [compojure.core :refer [context defroutes POST GET]]
    [malli.core :as mc]
    [ring.util.response :as response]
    [toucan2.core :as tc]
    [ubinote.api.common :as api]
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
  (-> (api/check-404 (tc/select-one :m/page :id id))
      (tc/hydrate :user [:annotations :comments])))

(defn- list-pages
  [_req]
  (-> (tc/select :m/page)
      (tc/hydrate :user)))

(defn- get-page-content
  "Returns the static file of the page"
  [id _req]
  (-> (api/check-404 (tc/select-one-fn :path :m/page :id id))
      (response/file-response {:root page/root})
      (response/content-type "text/html")))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           (GET "/content" [] (partial get-page-content id))))
