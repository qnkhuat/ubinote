(ns archiveio.controller.archive
  (:require [archiveio.controller.archive.path :as path]
            [archiveio.api.common :as api]
            [archiveio.cmd :as cmd]
            [archiveio.model.archive :refer [Archive]]
            [archiveio.model.common.schemas :as schemas]
            [net.cgrand.enlive-html :as html]
            [toucan.db :as db]
            [schema.core :as s]))

(defn extract-html
  "Extract metadata from a html file
  Currently return [:title]"
  [path]
  ;; TODO: find a way to extract description
  (let [parsed-doc (html/html-resource (java.io.File. path))
        title      (-> (html/select parsed-doc [:head :title])
                       first
                       :content
                       first)]
    {:title title}))

(def NewArchive
  {:user-id               s/Int
   :url                   schemas/URL
   (s/optional-key :tags) [s/Str]})

(s/defn create
  "Detect file type and archive file"
  [{:keys [url] :as archive} :- NewArchive]
  (let [{:keys [relative absolute]} (path/out-path url)
        domain                      (path/get-domain url)
        {:keys [err]}               (cmd/single-file url absolute)
        _                           (api/check-400 (= err "") {:url "Failed to download single-file"})
        {:keys [title]}             (extract-html absolute)]
    ;; TODO: move the file after download to name with title
    (db/insert! Archive (assoc archive
                               :domain domain
                               :path relative
                               :title title
                               :status "archived"))))
