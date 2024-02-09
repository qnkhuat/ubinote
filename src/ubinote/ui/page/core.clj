(ns ubinote.ui.page.core
  (:require
   [ubinote.ui.core :as ui]
   [ubinote.ui.template.core :as template]))

(def index
  (ui/layout
   [:div
    [:h1 "Hello, world!"]
    [:p "Welcome to this new world!"]]))

(def login
  (ui/layout
   [:div
    [:h1 "Login"]
    [:p "Please login to continue."]]))

(def not-found
  (template/html-response [:p "Not found :("]))

(def unauthorized
  (template/html-response
   [:div
    [:h1 "Unauthorized"]
    [:a {:href "/login"} "Login"]]))
