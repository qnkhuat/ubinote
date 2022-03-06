(ns archiveio.controller.comment
  (:require [archiveio.model.comment :refer [Comment]]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewComment
  {:user-id               s/Int
   :annotation-id         s/Int
   :content               s/Str
   })

(s/defn create
  "Detect file type and archive file"
  [comment :- NewComment]
  (db/insert! Comment comment))
