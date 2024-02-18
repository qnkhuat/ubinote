(ns ubinote.ui.page.core
  (:require
   [ubinote.ui.core :as ui]
   [ubinote.ui.template.core :as template]))

(def index
  (ui/html-response
   [:div {:class "container-fluid"}
    [:form {:id                           "new-page"
            :hx-swap                      "none"
            :hx-post                      "/api/page"
            (keyword "hx-on::after-swap") "this.reset()"}
     [:input {:type        "text"
              :placeholder " Archive a page"
              :name        "url"}]
     [:button {:class   "btn btn-primary"
               :type    "submit"
               :hx-swap "none"}
      "New"]]
    [:div {:id         "page-table"
           :hx-trigger "load, trigger-list-page from:body"
           :hx-get     "/api/page/html"}]]))

(def ^:private new-annotation-btn-id "ubinote-new-annotation-btn")
(def ^:private page-iframe-id "ubinote-page-content")

(defn view-page
  [page-id]
  (ui/html-response
   [:div {:class "position-relative"}
    [:iframe {:id          "ubinote-page-content"
              :title       "Ubinote page content"
              :scrolling   "no"
              :frameborder "0"
              :style       "width: 100%; display: flex;"
              :src         (format "/api/page/%d/content" page-id)
              :onload      (format "onIframeLoad(this, \"%s\")" new-annotation-btn-id)}]
    [:div {:hx-get      (format "/api/page/%d/annotation" page-id)
           ;; HACK, TODO: to get the annotation load after the page got loaded
           :hx-ext      "ubinote-swap-response"
           :hx-trigger  "load delay:500ms"}]
    [:div {:id         new-annotation-btn-id
           :class      "position-absolute z-3 bg-primary text-white"
           :style      "padding: 3px 8px; cursor: pointer;"
           :hx-ext     "ubinote-swap-response"
           :hx-post    "/api/annotation"
           :hx-vals    (format "js:{coordinate: %s,
                               page_id: %d}"
                               (format "fromRange(document.getElementById(\"%s\").contentWindow.document.body,
                                       document.getElementById(\"%s\").contentWindow.getSelection().getRangeAt(0))"
                                       page-iframe-id
                                       page-iframe-id)
                               page-id)
           :hx-trigger "click"}
     [:i {:class "bi bi-pencil"}]]]))

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
