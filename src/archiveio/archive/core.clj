(ns archiveio.archive.core
  (:require [archiveio.api.response :as resp]
            [archiveio.archive.path :as path]
            [archiveio.cmd :as cmd]
            [archiveio.model.archive :refer [Archive]]
            [net.cgrand.enlive-html :as html]
            [toucan.db :as db]))

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

(defn add
  "Detect file type and archive file"
  [url]
  (let [{:keys [relative absolute]} (path/out-path url)
        domain                      (path/get-domain url)
        {:keys [err]}               (cmd/single-file url absolute)
        _                           (resp/assert-400 (= err "") :url "Failed to download single-file")
        {:keys [title]}             (extract-html absolute)]
    ;; TODO: move the file after download to name with title
    (db/insert! Archive {:url    url
                         :path   relative
                         :domain domain
                         :title  title
                         :status "archived"})))
