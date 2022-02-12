(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.api.response :as resp]
            [taoensso.timbre :as log]))

(defn add-archive
  [{:keys [params] :as _request}]
  (let [{:keys [url]}      params
        out-path           (path/out-path url)
        {:keys [exit err]} (cmd/single-file url out-path)]
    (when-not (= exit 0)
      (log/error "Failed to download single-file" {:url url
                                                   :err err})
      (resp/error-response 400 "Failed to download single-file"))
    (resp/entity-response 200 {:url url
                               :out out-path})))

(defroutes routes
  (POST "/" [] add-archive))
