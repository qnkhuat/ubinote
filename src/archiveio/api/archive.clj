(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST GET]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.api.response :as resp]
            [archiveio.model.archive :refer [Archive]]
            [toucan.db :as db]))

(defn add-archive
  [{:keys [params] :as _request}]
  (let [{:keys [url]} params
        out-path      (path/out-path url)
        {:keys [err]} (cmd/single-file url out-path)]
    (resp/assert-400 (= err "") :url "Failed to download single-file")
    ;; TODO: save relative path instead
    (db/insert! Archive {:url  url
                         :path out-path})
    (resp/entity-response 200 {:url url
                               :out out-path})))

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/" [] list-archives))
