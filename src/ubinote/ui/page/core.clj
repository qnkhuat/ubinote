(ns ubinote.ui.page.core
  (:require
   [toucan2.core :as tc]
   [ubinote.ui.core :as ui]
   [ubinote.ui.template.core :as template]))

(def index
  (ui/html-response
   [:div {:class "container-fluid"}
    [:form {:id                "new-page"
            :hx-swap           "none"
            :hx-post           "/api/page"
            :hx-on--after-swap "this.reset()"}
     [:input {:type        "text"
              :placeholder " Archive a page"
              :name        "url"}]
     [:button {:class   "btn btn-primary"
               :type    "submit"
               :hx-swap "none"}
      "New"]]
    [:div {:id         "page-table"
           :hx-trigger "load, trigger-list-page from:body"
           :hx-get     "/api/page"}]]))

(def login
  (ui/html-response
   [:div {:id "login-page"}
    [:h1 "Login page"]
    [:form {:hx-post "/api/session"}
     [:label {:for "email"} "Email: "]
     [:input {:type "email" :name "email"}]
     [:label {:for "password"} "Password: "]
     [:input {:type "password" :name "password"}]
     [:button {:type "submit"} "Login"]]]
   :navbar? false
   :scripts false))

(def not-found
  (template/html-response [:p "Not found :("]))

(def unauthorized
  (template/html-response
   [:div
    [:h1 "Unauthorized"]
    [:a {:href "/login"} "Login"]]
   :navbar? false
   :scripts false))

(def ^:private new-annotation-btn-id "ubinote-new-annotation-btn")
(def ^:private page-iframe-id "ubinote-page-content")

(defn view-page*
  [{:keys [id public_uuid] :as page} public?]
  (ui/html-response
   [:div
    (when-not public?
      [:div {:id "ubinote-page-options"}
       [:nav {:class "navbar navbar-expand-lg bg-secondary"}
        [:div {:class "container-fluid text-light"}
         [:a {:class "navbar-nav text-light"
              :href  (:url page)}
          (:title page)]
         [:div {:class "d-flex"}
          [:div {:class "dropdown"}
           [:button {:class "btn dropdown-toggle"
                     :data-bs-toggle "dropdown"
                     :data-bs-auto-close "false"
                     :type  "button"}
            [:i {:class "bi bi-share"}]]
           [:ul {:class "dropdown-menu dropdown-menu-end"}
            [:li {:class "dropdown-item"}
             (if-let [uuid (:public_uuid page)]
               (ui/render :page-public-link {:id id :public_uuid uuid})
               (ui/render :page-create-public-link {:id id}))]]]]]]])
    [:div {:id    "ubinote-page-content-wrapper"
           :class "position-relative"}
     [:iframe {:id          "ubinote-page-content"
               :title       "Ubinote page content"
               :scrolling   "no"
               :frameborder "0"
               :style       "width: 100%; display: flex; position: relative;"
               :src         (if-not public? (format "/api/page/%d/content" id) (format "/api/public/page/%s/content" public_uuid))
               :onload      (format "onIframeLoad(this, \"%s\", %s)" new-annotation-btn-id public?)}]
     [:div {:hx-get      (if-not public? (format "/api/page/%d/annotation" id) (format "/api/public/page/%s/annotation" public_uuid))
            ;; HACK, TODO: to get the annotation load after the page got loaded
            :hx-ext      "ubinote-swap-response"
            :hx-trigger  "load delay:500ms"}]
     (when-not public?
       [:div {:id         new-annotation-btn-id
              :class      "position-absolute z-3 bg-primary text-white"
              :style      "padding: 3px 8px; cursor: pointer;"
              :hx-ext     "ubinote-swap-response"
              :hx-post    "/api/annotation"
              :hx-on--after-request
              (format "this.style.visibility = 'hidden'; document.getElementById(\"%s\").contentWindow.getSelection().empty()"
                      page-iframe-id)
              :hx-vals    (format "js:{coordinate: fromRange(document.getElementById(\"%s\").contentWindow.document.body,
                                  document.getElementById(\"%s\").contentWindow.getSelection().getRangeAt(0)),
                                  page_id: %d}"
                                  page-iframe-id
                                  page-iframe-id
                                  id)
              :hx-trigger "click"}
        [:i {:class "bi bi-pencil"}]])]]))

(defn view-page
  [page-id _req]
  (view-page* (tc/select-one :m/page page-id) false))

(defn view-page-public
  [page-uuid _req]
  (if-let [page #p (tc/select-one :m/page :public_uuid page-uuid)]
    (view-page* page true)
    not-found))
