(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST GET]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.api.response :as resp]
            [archiveio.model.archive :refer [Archive]]
            [toucan.db :as db]))

(defn add-archive
  [{:keys [params] :as _request}]
  (let [{:keys [url]}               params
        {:keys [relative absolute]} (path/out-path url)
        {:keys [err]}               (cmd/single-file url absolute)]
    (resp/assert-400 (= err "") :url "Failed to download single-file")
    (resp/entity-response 200 (db/insert! Archive {:url  url
                                                   :path relative
                                                   :status "archived"}))))

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/" [] list-archives))
