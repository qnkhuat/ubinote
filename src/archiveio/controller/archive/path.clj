(ns archiveio.controller.archive.path
  (:require [archiveio.util.fs :as fs]
            [archiveio.config :as cfg]
            [archiveio.util.b64 :as b64]
            [archiveio.model.common.schemas :as schemas]
            [clojure.string :as string]))

;; root to store archive files
;; TODO: make sure it's exists, is a folder and writable
(def root (fs/absolute (cfg/config-str :aio-root)))

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

(defn get-filename
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
  The out folder will have the path: root/domain/{year}{month}{date}_{hour}{minute}{second}_{filename}.{ext}"
  [url]
  (let [domain          (get-domain url)
        fname           (get-filename url)
        dir             (fs/path-join root domain)
        rel-path        (fs/path-join domain fname)
        abs-path        (fs/path-join dir fname)]
    (assert (some? domain) "Domain not found")
    (fs/make-dirs dir)
    (when (fs/exists? abs-path)
      (throw (ex-info "Path already existed" {:path abs-path})))
    (when-not (fs/exists? dir)
      (throw (ex-info "Failed to out folder" {:url url})))
    {:relative rel-path
     :absolute abs-path}))
