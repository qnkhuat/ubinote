(ns ubinote.ui.page.core
  (:require
   [ubinote.ui.core :as ui]
   [ubinote.ui.template.core :as template]))

(def index
  (ui/layout
   [:div
    [:nav {:class "navbar navbar-expand-lg bg-dark"}
     [:div {:class "container-fluid"}
      [:a {:class "navbar-brand text-light"
           :href  "/"}
       "Ubinote"]
      [:div {:class "d-flex"}
       [:a {:class "text-light text-decoration-none"
            :href  "/user"} "User"]]]]
    [:div {:class "container"}
     [:form {:id      "add-page"
             :hx-swap "none"
             :hx-post "/api/page"}
      [:input {:type "text"
               :name "url"}]
      [:button {:class   "btn btn-primary"
                :type    "submit"
                :hx-swap "none"}
       "New"]]
     [:div {:id         "page-table"
            :hx-trigger "load, trigger-new-page from:body"
            :hx-get     "/api/page/html"}
      "sup"]]]))

(def login
  (ui/layout
   [:div {:id "login-page"}
    [:h1 "Login page"]
    [:form {:hx-post "/api/session"}
     [:label {:for "email"} "Email: "]
     [:input {:type "email" :name "email"}]
     [:label {:for "password"} "Password: "]
     [:input {:type "password" :name "password"}]
     [:button {:type "submit"} "Login"]]]))

(def not-found
  (template/html-response [:p "Not found :("]))

(def unauthorized
  (template/html-response
   [:div
    [:h1 "Unauthorized"]
    [:a {:href "/login"} "Login"]]))
