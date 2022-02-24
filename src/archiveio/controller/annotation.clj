(ns archiveio.controller.annotation
  (:require [archiveio.model.annotation :refer [Annotation]]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewAnnotation
  {:user-id               s/Int
   :archive-id            s/Int
   :color                 s/Str
   :coordinate            s/Str
   })

(s/defn create
  "Detect file type and archive file"
  [annotation :- NewAnnotation]
  (db/insert! Annotation annotation))
