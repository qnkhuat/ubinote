(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as apath]
            [ring.util.response :as resp]))

(defn add-archive
  [{:keys [params] :as request}]
  (let [{:keys [url]} params
        out-dir       (apath/out-dir url)]
    (cmd/single-file url (str out-dir "adfsf.html"))
    (resp/response {:url url})))

(defroutes routes
  (POST "/" [] add-archive))
