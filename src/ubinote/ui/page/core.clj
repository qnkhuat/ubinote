(ns ubinote.ui.page.core
  (:require
   [ubinote.ui.core :as ui]
   [ubinote.ui.template.core :as template]))

(def index
  (ui/html-response
   [:div
    [:form {:id      "add-page"
            :hx-swap "none"
            :hx-post "/api/page"
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


(defn view-page
  [page-id]
  (ui/html-response
   [:div
    [:iframe {:id          "ubinote-page-content"
              :title       "Ubinote page content"
              :scrolling   "no"
              :frameborder "0"
              :style       "width: 100%; display: flex;"
              :src         (format "/api/page/%d/content" page-id)
              :onload      "onIframeLoad(this)"}]
    [:div {}]]))

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
