(ns archiveio.api.archive
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.cmd :as cmd]
            [archiveio.archive.path :as path]
            [archiveio.archive.core :as archive]
            [archiveio.api.response :as resp]
            [archiveio.model.archive :refer [Archive]]
            [toucan.db :as db]))

(defn add-archive
  [{:keys [params] :as _req}]
  (let [{:keys [url]} params]
    (resp/entity-response 200 (archive/add url))))

(defn get-archive
  [id _req]
  (let [archive (db/select-one Archive :id id)]
    (resp/assert-404 archive "Archive not found")
    (resp/entity-response 200 archive)))

(defn list-archives
  [_req]
  (resp/entity-response 200 (db/select Archive)))

(defroutes routes
  (POST "/" [] add-archive)
  (GET "/" [] list-archives)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-archive id))))
