(ns ubinote.ui.core)

(defmulti render
  (fn [_data component-name]
    component-name))
