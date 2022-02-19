(ns archiveio.archive.core
  (:require [archiveio.api.response :as resp]
            [archiveio.archive.path :as path]
            [archiveio.cmd :as cmd]
            [archiveio.model.archive :refer [Archive]]
            [net.cgrand.enlive-html :as html]
            [toucan.db :as db]))

(defn extract-html
  "Extract metadata from a html file
  Current return [:title :description]"
  [path]
  ;; TODO: find a way to extract description
  (let [; with a simple test I found that hickory parser is faster than elive. but feel free to try it again
        parsed-doc (html/html-resource (java.io.File. path))
        title      (-> (html/select parsed-doc [:head :title])
                       first
                       :content
                       first)]
    {:title title}))

(extract-html "/Users/earther/fun/4_archiveio/.archiveio/en.wikipedia.org/20220220_010907_aHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvS2VyZXLFqw==.html")

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
