(ns ubinote.ui.core)

(defmulti render
  (fn [_data component-name]
    component-name))

(defmulti css identity)

#_(defmulti css :a
    [component-name])

