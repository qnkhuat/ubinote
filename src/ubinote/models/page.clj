(ns ubinote.models.page
  (:require [clojure.string :as string]
            [ubinote.cmd :as cmd]
            [ubinote.util.fs :as fs]
            [ubinote.util.b64 :as b64]
            [ubinote.api.common :as api]
            [ubinote.config :as cfg]
            [net.cgrand.enlive-html :as html]
            [toucan.models :as models]
            [toucan.db :as db]))

;; ------------------------------- Toucan helpers -------------------------------

(models/defmodel Page :page)

(extend (class Page)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:timestamped? true})}))

;; ------------------------------- Create page fns -------------------------------

;; root to store page
;; TODO: make sure it's exists, is a folder and writable
(def root (fs/absolute (cfg/config-str :root)))

;; https://stackoverflow.com/a/25703406
(def url-regex #"^(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/?\n]+)")

(defn get-domain
  [url]
  (second (re-find url-regex url)))

(defn get-ext
  "Inference document's extension from url"
  [url]
  (cond
   (string/ends-with? url ".pdf") ".pdf"
   :else                          ".html"))

(defn format-filename
  "Generate filename for a given url with format {year}{month}{date}_{hour}{minute}{second}_{base64(url)}.{ext}"
  [url]
  (let [ext     (get-ext url)
        now     (java.time.LocalDateTime/now)
        year    (str (.getYear now))
        month   (format "%02d" (.getMonthValue now))
        date    (format "%02d" (.getDayOfMonth now))
        hour    (format "%02d" (.getHour now))
        minute  (format "%02d" (.getMinute now))
        sec     (format "%02d" (.getSecond now))]
    (str year month date "_" hour minute sec "_" (b64/encode url) ext)))

(defn out-path
  "Get out path to save an url and create the folder if not exists
  The out folder will have the path: root/domain/{filename}"
  [url]
  (let [domain   (get-domain url)
        fname    (format-filename url)
        dir      (fs/path-join root domain)
        rel-path (fs/path-join domain fname)
        abs-path (fs/path-join dir fname)]
    (api/check-400 (some? domain) "Domain not found")
    (fs/make-dirs dir)
    (when (fs/exists? abs-path)
      (throw (ex-info "Path already existed" {:path abs-path})))
    (when-not (fs/exists? dir)
      (throw (ex-info "Failed to out folder" {:url url})))
    {:relative rel-path
     :absolute abs-path}))

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

(defn create-page
  "Detect file type and page file"
  [{:keys [url] :as new-page}]
  (let [{:keys [relative absolute]} (out-path url)
        domain                      (get-domain url)
        {:keys [err]}               (cmd/single-file url absolute)
        _                           (api/check-400 (= err "") {:url "Failed to download single-file"})
        {:keys [title]}             (extract-html absolute)]
    ;; TODO: move the file after download to name with title
    (db/insert! Page (assoc new-page
                            :domain domain
                            :path relative
                            :title title
                            :status "archived"))))
