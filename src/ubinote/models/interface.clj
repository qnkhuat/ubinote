(ns ubinote.models.interface
  (:require
   [cheshire.core :as json]
   [toucan2.core :as tc]))

(def json-in json/generate-string)

(def json-out (fn [x] (json/parse-string x keyword)))

(tc/deftransforms :tf/json
  {:name {:in  json/generate-string
          :out #(json/parse-string % keyword)}})
