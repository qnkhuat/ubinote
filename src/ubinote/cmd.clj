(ns ubinote.cmd
  (:require [ubinote.util.fs :as fs]
            [clojure.string :as string]
            [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]))

(defn which
  "like `which` command"
  [exe]
  (if (fs/absolute? exe)
    (fs/executable? exe)
    (fs/find-in (-> (fs/env-path)
                    (string/split (re-pattern fs/path-separator)))
                exe fs/executable?)))

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
  "download an url as a single fs with name is the page {title}-{time-locale}.html"
  ([url]
   (single-file url nil))

  ([url out-path]
   {:pre [(fs/absolute? out-path)]}
   ;; TODO maybe call an OPTIONs to the endpoint to check if it's reachable
   (let [chrome-bin      (find-chrome-binary)
         single-file-bin (which "single-file")
         ;; https://github.com/gildas-lormeau/SingleFile/tree/master/cli
         ;; to install npm install -g "gildas-lormeau/single-file-cli"
         args (filter some? [single-file-bin (format "--browser-executable-path=%s" chrome-bin)
                             (format "--filename-template={page-title}-{time-locale}.html")
                             url out-path])
         _    (assert chrome-bin "Could not find `CHROME_BINARY` in your system")
         _    (assert single-file-bin "Could not find `single-file` in your system")
         out  (apply sh args)]
     (log/info ::single-file out)
     out)))
