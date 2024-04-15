(ns ubinote.ui
  (:require
   [potemkin :as p]
   [ubinote.ui.template :as template]))

(p/import-vars
 [template
  html-response
  render-hiccup-fragment
  with-nav-bar])

(defmulti render
  (fn [component _data]
    component))

(defmethod render :default
  [component _data]
  (throw (ex-info "No render implementation for:" component {:component component})))
