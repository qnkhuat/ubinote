(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.api.response :as resp]))

(defn add-archive
  [{:keys [params] :as _request}]
  (let [{:keys [url]} params
        out-path      (path/out-path url)
        {:keys [err]} (cmd/single-file url out-path)]
    (resp/assert-400 (= err "") :url "Failed to download single-file")
    (resp/entity-response 200 {:url url
                               :out out-path})))

(defroutes routes
  (POST "/" [] add-archive))
