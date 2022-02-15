(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST GET]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.api.response :as resp]
            [archiveio.model.archive :refer [Archive]]
            [toucan.db :as db]))

(defn add-archive
  [{:keys [params] :as _req}]
  (let [{:keys [url]}               params
        {:keys [relative absolute]} (path/out-path url)
        {:keys [err]}               (cmd/single-file url absolute)]
    (resp/assert-400 (= err "") :url "Failed to download single-file")
    (resp/entity-response 200 (db/insert! Archive {:url  url
                                                   :path relative
                                                   :status "archived"}))))

(defn get-archive
  [{:keys [params] :as _req}]
  (let [id (Integer/parseInt (:id params))
        res (db/select-one Archive :id id)]
    (resp/assert-400 res :id (format "Archive with id %d not found" id))
    (resp/entity-response 200 res)))

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/:id" [] get-archive)
  (GET "/" [] list-archives))
