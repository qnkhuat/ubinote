(ns ubinote.ui.core
  (:require
   [potemkin :as p]
   [ubinote.ui.template.core :as template]))

(p/import-vars
 [template
  html-response
  hiccup->html-response
  with-nav-bar])

(defmulti render
  (fn [_data component-name]
    component-name))
