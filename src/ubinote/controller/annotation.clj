(ns ubinote.controller.annotation
  (:require [ubinote.model.annotation :refer [Annotation]]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewAnnotation
  {:user-id    s/Int
   :page-id    s/Int
   :color      s/Str
   :coordinate s/Str
   })

(s/defn create
  "Detect file type and page file"
  [annotation :- NewAnnotation]
  (db/insert! Annotation annotation))
