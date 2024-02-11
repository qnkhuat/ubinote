(ns ubinote.api.page
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes DELETE POST GET]]
   [malli.core :as mc]
   [ring.util.response :as response]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.models.common.schema :as schema]
   [ubinote.models.page :as page]
   [ubinote.ui.core :as ui]))

(def NewPage
  (mc/schema
   [:map
    [:url                         schema/URL]
    [:creator_id                  schema/IntegerGreaterThanZero]
    [:tags       {:optional true} [:sequential :string]]]))

(defn- add-page
  [{:keys [body] :as _req}]
  (->> (assoc body :creator_id api.u/*current-user-id*)
       (api.u/validate NewPage)
       page/create-page))

(defn- get-page
  [id _req]
  (-> (api.u/check-404 (tc/select-one :m/page :id id))
      (tc/hydrate :user [:annotations :comments])))

(defn- list-pages
  [_req]
  (tc/select :m/page))

(defmethod ui/render :pages-table
  [data _component-name]
  (def data data)
  [:table {:class "table table-hover"}
   [:thead
    [:tr
     [:th "Title"]
     [:th "Domain"]
     [:th "URL"]
     [:th "Last Updated"]
     [:th "Delete"]]]
   [:tbody {:hx-confirm "Are you sure?"}
    (for [page data]
      [:tr
       [:td [:a {:href (format "/page/%d" (:id page))} (:title page)]]
       [:td (:domain page)]
       [:td [:a {:href (:url page)} (:title page)]]
       [:td (str (:updated_at page))]
       [:td [:button {:hx-delete (format "/api/page/%d" (:id page))
                      :class     "btn btn-danger"
                      :hx-swap   "delete"}
             "DELETE"]]])]])

(defn- list-pages-html
  [_req]
  (-> (tc/select :m/page)
      (ui/render :pages-table)
      ui/hiccup->html-response))

(defn- get-page-content
  "Returns the static file of the page"
  [id _req]
  (-> (api.u/check-404 (tc/select-one-fn :path :m/page :id id))
      (response/file-response {:root page/root})
      (response/content-type "text/html")
      (response/header "X-Frame-Options" "SAMEORIGIN")))

(defn- public-page
  [id _req]
  (let [page (api.u/check-404 (tc/select-one :m/page :id id))
        uuid (str (java.util.UUID/randomUUID))]
    (when (:public_uuid page)
      (throw (ex-info "Page is already public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid uuid})
    uuid))

(defn- disable-public
  [id _req]
  (let [page (api.u/check-404 (tc/select-one :m/page :id id))]
    (when-not (:public_uuid page)
      (throw (ex-info "Page is not public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid nil})
    api.u/generic-204-response))

(defn- delete-page
  [id _req]
  (-> (api.u/check-404 (tc/select-one :m/page :id id))
      (page/delete-page))
  api.u/generic-204-response)

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (GET "/html" [] list-pages-html)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))
           (DELETE "/" [] (partial delete-page id))
           (POST "/public" [] (partial public-page id))
           (DELETE "/public" [] (partial disable-public id))
           (GET "/content" [] (partial get-page-content id))))
