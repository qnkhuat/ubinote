(ns ubinote.ui
  (:require
   [potemkin :as p]
   [ubinote.ui.template :as template]))

(p/import-vars
 [template
  html-response
  hiccup->html-response
  with-nav-bar])

(defmulti render
  (fn [component _data]
    component))
