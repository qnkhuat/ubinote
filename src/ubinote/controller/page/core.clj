(ns ubinote.controller.page.core
  (:require [ubinote.controller.page.path :as path]
            [ubinote.api.common :as api]
            [ubinote.cmd :as cmd]
            [ubinote.model.page :refer [Page]]
            [ubinote.model.common.schemas :as schemas]
            [net.cgrand.enlive-html :as html]
            [toucan.db :as db]
            [schema.core :as s]))

(defn extract-html
  "Extract metadata from a html file
  Currently return [:title]"
  [path]
  ;; TODO: find a way to extract description
  (let [parsed-doc (html/html-resource (java.io.File. path))
        ;; Is this the perfect way to get title?
        title      (-> (html/select parsed-doc [:head :title])
                       first
                       :content
                       first)]
    {:title title}))

(def NewPage
  {:user-id               s/Int
   :url                   schemas/URL
   (s/optional-key :tags) [s/Str]})

(s/defn create
  "Detect file type and page file"
  [{:keys [url] :as page} :- NewPage]
  (let [{:keys [relative absolute]} (path/out-path url)
        domain                      (path/get-domain url)
        {:keys [err]}               (cmd/single-file url absolute)
        _                           (api/check-400 (= err "") {:url "Failed to download single-file"})
        {:keys [title]}             (extract-html absolute)]
    ;; TODO: move the file after download to name with title
    (db/insert! Page (assoc page
                            :domain domain
                            :path relative
                            :title title
                            :status "archived"))))
