(ns archiveio.controller.archive
  (:require [archiveio.model.archive :refer [Archive]]
            [archiveio.model.common.schemas :as schemas]
            [toucan.db :as db]
            [schema.core :as s]))


(def NewArchive
  {})

(s/defn create-archive
  [new-archive :- NewArchive]
  (db/insert! Archive new-archive))

(create-archive
  {:user-id 1
   :url "abc.com"
   :domain "abc.com"
   :path "afdsfsdf/sfsdf/sdf/sd.clj"
   :title "How to be a millionare"
   :status "archived"
   })
