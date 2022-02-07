(ns archiveio.archive.core
  (:require [archiveio.cmd.which :refer [which] :as cmd-which]
            [clojure.java.shell :refer [sh]]))

(defn find-chrome-binary
  []
  (first (filter (fn [path] (which path))
                 ["chromium-browser"
                  "chromium"
                  "/Applications/Chromium.app/Contents/MacOS/Chromium"
                  "chrome"
                  "google-chrome"
                  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
                  "google-chrome-stable"
                  "google-chrome-beta"
                  "google-chrome-canary"
                  "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"
                  "google-chrome-unstable"
                  "google-chrome-dev"])))

(defn single-file
  [url out-path]
  {:pre [(cmd-which/absolute? out-path)]}
  (let [chrome-bin (find-chrome-binary)
        single-file-bin (which "single-file")
        ;; https://github.com/gildas-lormeau/SingleFile/tree/master/cli
        cmd [single-file-bin (format "--browser-executable-path=%s" chrome-bin)
             url out-path]]
    (assert chrome-bin "Could not find `CHROME_BINARY` in your system")
    (assert single-file-bin "Could not find `single-file` in your system")
    (apply sh cmd)))

(defn pdf
  [url]
  nil)
