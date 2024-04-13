(ns ubinote.api.public
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ring.util.response :as response]
   [toucan2.core :as tc]
   [ubinote.api.page :as api.page]
   [ubinote.api.util :as api.u]
   [ubinote.models.page :as page]
   [ubinote.ui :as ui]))

(defn get-public-page
  [uuid _req]
  (-> (api.u/check-404 (tc/select-one :m/page :public_uuid uuid))
      (tc/hydrate [:annotations :comments])))

(defn- get-public-page-content
  "Returns the static file of the page"
  [uuid _req]
  (-> (api.u/check-404 (tc/select-one-fn :path :m/page :public_uuid uuid))
      (response/file-response {:root page/root})
      (response/content-type "text/html")
      (response/header "X-Frame-Options" "SAMEORIGIN")))

(defmethod ui/render :public-annotation
  [_component annotation]
  (api.page/render-annotation annotation true))

(defn- get-public-page-annotation
  [uuid _req]
  (->> (tc/hydrate (tc/select :m/annotation :page_id (tc/select-one-pk :m/page :public_uuid uuid)) :comments)
       (map #(ui/render :public-annotation %))
       ui/hiccup->html-response))

(defroutes routes
  (context "/page" []
           (GET "/:uuid" [uuid] (partial get-public-page uuid))
           (GET "/:uuid/annotation" [uuid] (partial get-public-page-annotation uuid))
           (GET "/:uuid/content" [uuid] (partial get-public-page-content uuid))))
