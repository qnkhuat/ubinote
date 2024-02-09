(ns ubinote.ui.page.core
  (:require
   [ubinote.api.common :as api.common]
   [ubinote.ui.template.core :as template]))

(def index
  (-> (template/layout
       [:div
        [:h1 "Hello, world!"]
        [:p "Welcome to this new world!"]])
      api.common/html))

(def login
  (-> (template/layout
       [:div
        [:h1 "Login"]
        [:p "Please login to continue."]])
      api.common/html))
