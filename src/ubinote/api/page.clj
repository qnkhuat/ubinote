(ns ubinote.api.page
  (:require
   [cheshire.core :as json]
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes DELETE POST GET]]
   [malli.core :as mc]
   [ring.util.response :as response]
   [toucan2.core :as tc]
   [ubinote.api.util :as api.u]
   [ubinote.models.annotation :as annotation]
   [ubinote.models.common.schema :as schema]
   [ubinote.models.page :as page]
   [ubinote.ui :as ui]
   [ubinote.util :as u]))

(def NewPage
  (mc/schema
   [:map
    [:url                         schema/URL]
    [:creator_id                  schema/IntegerGreaterThanZero]
    [:tags       {:optional true} [:sequential :string]]]))

(defn- add-page
  [{:keys [params] :as _req}]
  (let [page (->> (assoc params :creator_id api.u/*current-user-id*)
                  (api.u/decode NewPage)
                  page/create-page!)]
    (api.u/htmx-trigger page "trigger-list-page")))

(defn- get-page-annotation
  [id _req]
  (->> (tc/hydrate (tc/select :m/annotation :page_id id) :comments)
       (map #(ui/render :annotation %))
       ui/render-hiccup-fragment))

(defn render-annotation
  "Options:
  - public?: whether the annotation will be rended on a public page?"
  [{:keys [id coordinate comments] :as _annotation} public?]
  [:span
   {;; custom attribute handled by `ubinote-swap-response` extension
    :ubinote-annotation-coordinate (json/generate-string coordinate)
    :ubinote-annotation-id         id
    :class                         (annotation/annotation-color-class (seq comments))}
   ;; the popover when click on highlight
   [:div {:class "border border-black rounded bg-white p-2 position-relative"
          :style "width: 400px;"}
    (when-not public?
      [:div {:class "d-flex justify-content-end pb-2"}
       [:button {:hx-delete            (format "/api/annotation/%d" id)
                 :hx-on--after-request (format "deleteAnnotation(%d)" id)
                 :hx-confirm           "Are you sure?"
                 :hx-swap              "none"
                 :hx-trigger           "click"
                 :class                "btn btn-sm btn-danger"}
        [:i {:class "bi bi-trash"}]]])
    [:div {:class "comments-and-form"}
     [:div {:class "comments"}
      (map #(ui/render :annotation-comment %) comments)
      (when (and public? (zero? (count comments)))
        [:p "No comments"])]
     (when-not public?
       [:form {:hx-post              "/api/comment"
               :hx-vals              (json/generate-string {:annotation_id id})
               :hx-on--after-request "this.reset()"
               :hx-target            "previous .comments"
               :hx-swap              "beforeend"
               :hx-trigger           "submit"
               :class                "overflow-hidden"}
        [:textarea {:name        "content"
                    :style       "height: 100px;"
                    :class       "w-100 mb-2"
                    :placeholder "Something interesting"}]
        [:button {:type "submit" :class "btn btn-sm btn-primary float-end"}
         [:i {:class "bi bi-send"}]]])]]])

(defmethod ui/render :annotation
  [_component annotation]
  (render-annotation annotation false))

(defmethod ui/render :pages-table
  [_component data]
  [:table {:class "table table-hover"}
   [:thead
    [:tr
     [:th "Title"]
     [:th "Domain"]
     [:th "URL"]
     [:th "Created At"]
     [:th "Delete"]]]
   [:tbody {:hx-confirm "Are you sure?"
            :hx-swap    "outerHTML"
            :hx-target  "closest tr"}
    (for [page data]
      [:tr {:class "page-row"}
       [:td [:a {:href (format "/page/%d" (:id page))} (:title page)]]
       [:td (:domain page)]
       [:td [:a {:href (:url page)} (:url page)]]
       [:td (u/timestamp->ago-text (:created_at page))]
       [:td [:button {:hx-delete (format "/api/page/%d" (:id page))
                      :class     "btn btn-danger"}
             [:i {:class "bi bi-trash"}]]]])]])

(defn- list-pages
  [_req]
  (->> (tc/select :m/page {:order-by [[:created_at :desc]]})
       (ui/render :pages-table)
       ui/render-hiccup-fragment))

(defn- get-page-content
  "Returns the static file of the page"
  [id _req]
  (-> (api.u/check-404 (tc/select-one-fn :path :m/page :id id))
      (response/file-response {:root page/root})
      (response/content-type "text/html")
      (response/header "X-Frame-Options" "SAMEORIGIN")))

(defmethod ui/render :page-public-link
  [_component {:keys [public_uuid id]}]
  (let [public-age-url (str "/public/page/" public_uuid)]
    [:div {:id "ubinote-create-public-link"
           :hx-trigger :click
           :hx-get  "/api/age"}
     "Public link: "
     [:a {:href    public-age-url
          :x-data (format "{publicPageURL: \"%s\"}" public-age-url)
          :x-init "publicPageURL = `${window.location.protocol}//${window.location.host}` + publicPageURL "}
      [:span {:x-text "publicPageURL"}]]
     [:button {:class      "btn"
               :hx-trigger "click"
               :hx-target  "#ubinote-create-public-link"
               :hx-delete  (format "/api/page/%d/public" id)}
      "Disable"]]))

(defmethod ui/render :page-create-public-link
  [_component {:keys [id]}]
  [:button {:class      "btn"
            :hx-trigger "click"
            :hx-swap    "outerHTML"
            :hx-post    (format "/api/page/%d/public" id)}
   "Create public link"])

(defn- public-page
  [id _req]
  (let [page (api.u/check-404 (tc/select-one :m/page :id id))
        uuid (str (java.util.UUID/randomUUID))]
    (when (:public_uuid page)
      (throw (ex-info "Page is already public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid uuid})
    (ui/render-hiccup-fragment (ui/render :page-public-link {:public_uuid uuid :id id}))))

(defn- disable-public
  [id _req]
  (let [page (api.u/check-404 (tc/select-one :m/page :id id))]
    (when-not (:public_uuid page)
      (throw (ex-info "Page is not public" {:status-code 400})))
    (tc/update! :m/page id {:public_uuid nil})
    (ui/render-hiccup-fragment
     [:button {:class "btn"
               :hx-trigger "click"
               :hx-swap    "outerHTML"
               :hx-post    (format "/api/page/%d/public" id)}
      "Create public link"])))

(defn- delete-page
  [id _req]
  (-> (api.u/check-404 (tc/select-one :m/page :id id))
      (page/delete-page))
  api.u/generic-200-response)

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/annotation" [] (partial get-page-annotation id))
           (DELETE "/" [] (partial delete-page id))
           (POST "/public" [] (partial public-page id))
           (DELETE "/public" [] (partial disable-public id))
           (GET "/content" [] (partial get-page-content id))))
