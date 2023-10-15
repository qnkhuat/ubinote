(ns ubinote.api.page
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes DELETE POST GET]]
   [malli.core :as mc]
   [ring.util.response :as response]
   [toucan2.core :as tc]
   [ubinote.api.common :as api]
   [ubinote.models.common.schema :as schema]
   [ubinote.models.page :as page]))

(def NewPage
  (mc/schema
   [:map
    [:url                         schema/URL]
    [:creator_id                  schema/IntegerGreaterThanZero]
    [:tags       {:optional true} [:sequential :string]]]))

(defn- add-page
  [{:keys [body] :as _req}]
  (-> (assoc body :creator_id api/*current-user-id*)
      (api/validate NewPage)
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
      (response/content-type "text/html")
      (response/header "X-Frame-Options" "SAMEORIGIN")))

(defn- public-page
  [id _req]
  (let [page (api/check-404 (tc/select-one :m/page :id id))
        uuid (str (java.util.UUID/randomUUID))]
    (when (:public_uuid page)
      (throw (ex-info "Page is already public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid uuid})
    uuid))

(defn- disable-public
  [id _req]
  (let [page (api/check-404 (tc/select-one :m/page :id id))]
    (when-not (:public_uuid page)
      (throw (ex-info "Page is not public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid nil})
    api/generic-204-response))

(defn- delete-page
  [id _req]
  (-> (api/check-404 (tc/select-one :m/page :id id))
      (page/delete-page))
  api/generic-204-response)

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           (DELETE "/" [] (partial delete-page id))
           (POST "/public" [] (partial public-page id))
           (DELETE "/public" [] (partial disable-public id))
           (GET "/content" [] (partial get-page-content id))))
