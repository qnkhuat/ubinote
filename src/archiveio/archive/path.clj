(ns archiveio.archive.path
  (:require [archiveio.util.fs :as fs]))

(def root (fs/absolute ".archiveio"))

(defn get-domain
  [url]
  ;; https://stackoverflow.com/a/25703406
  (second (re-find (re-pattern #"^(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/?\n]+)") url)))

(defn out-dir
  "Get out path to save an url and create the folder if not exists"
  [url]
  (let [domain (get-domain url)
        now    (java.time.LocalDate/now)
        year   (str (.getYear now))
        month  (str (.getMonthValue now))
        date   (str (.getDayOfMonth now))
        dir    (fs/path-join root domain year month date)]
    (fs/make-dirs dir)
    dir))
