(ns ubinote.api.public
  (:require
    [compojure.coercions :refer [as-int]]
    [compojure.core :refer [context defroutes DELETE POST GET]]
    [malli.core :as mc]
    [ring.util.response :as response]
    [toucan2.core :as tc]
    [ubinote.api.common :as api]
    [ubinote.api.page]
    [ubinote.models.common.schema :as schema]
    [ubinote.models.page :as page]))

(defn get-public-page
  [uuid _req]
  (println "GETTING PUBLIC PAGE")
  (-> (api/check-404 (tc/select-one :m/page :public_uuid uuid))
      (tc/hydrate :annotations)))

(defn- get-public-page-content
  "Returns the static file of the page"
  [uuid _req]
  (-> (api/check-404 (tc/select-one-fn :path :m/page :public_uuid uuid))
      (response/file-response {:root page/root})
      (response/content-type "text/html")))

(defroutes routes
  (context "/page" []
           (GET "/:uuid" [uuid] (partial get-public-page uuid))
           (GET "/:uuid/content" [uuid] (partial get-public-page-content uuid))))
