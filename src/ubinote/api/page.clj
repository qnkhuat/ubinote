(ns ubinote.api.page
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [ubinote.api.common :as api]
            [ubinote.cmd :as cmd]
            [ubinote.model.page :refer [Page] :as m-page]
            [ubinote.model.annotation :refer [Annotation]]
            [ubinote.model.common.schemas :as schemas]
            [net.cgrand.enlive-html :as html]
            [toucan.db :as db]
            [toucan.hydrate :refer [hydrate]]
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

(s/defn create-page
  "Detect file type and page file"
  [{:keys [url] :as page} :- NewPage]
  (let [{:keys [relative absolute]} (m-page/out-path url)
        domain                      (m-page/get-domain url)
        {:keys [err]}               (cmd/single-file url absolute)
        _                           (api/check-400 (= err "") {:url "Failed to download single-file"})
        {:keys [title]}             (extract-html absolute)]
    ;; TODO: move the file after download to name with title
    (db/insert! Page (assoc page
                            :domain domain
                            :path relative
                            :title title
                            :status "archived"))))

(def validate-add-page
  (s/validator NewPage))

(defn hydration
  [results]
  (hydrate results :user))

(defn add-page
  [{:keys [params] :as _req}]
  (validate-add-page params)
  ;; TODO, :user-id should take from session
  (create-page params))

(defn get-page
  [id _req]
  (let [page (-> (db/select-one Page :id id)
                 (hydrate :user :annotations
                          [:annotations :comments]))]
    (api/check-404 page)
    page))

(defn list-pages
  [_req]
  (-> (db/select Page)
      (hydrate :user)))

(defn get-annotation
  [id _req]
  (-> (db/select Annotation :page-id id)
      (hydrate :an)))

(defroutes routes
  (POST "/" [] add-page)
  (GET "/" [] list-pages)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-page id))))
