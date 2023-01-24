(ns ubinote.cmd
  (:require
    [clojure.core.memoize :as memoize]
    [clojure.java.shell :refer [sh]]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [ubinote.config :as cfg]
    [ubinote.util.fs :as fs]))

(def ^:private which
  "like `which` command"
  (memoize/ttl
    (fn [exe]
      (some-> (sh "which" exe)
              :out
              str/trim))
    :ttl/threshold (* 1000 1)))

(def ^:private find-chrome-binary
  (memoize/ttl
    (fn []
      (first (filter (fn [path]
                       (let [exe (which path)]
                         (and (not (str/blank? exe)) (fs/executable? exe))))
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
    :ttl/threshold (* 1000 1)))

(defn single-file
  "download an url as a single fs with name is the page {title}-{time-locale}.html"
  ([url]
   (single-file url nil))

  ([url out-path]
   {:pre [(fs/absolute? out-path)]}
   ;; TODO maybe call an OPTIONs to the endpoint to check if it's reachable
   (let [chrome-bin      (find-chrome-binary)
         single-file-bin (or
                           (cfg/config-str :single-file-bin)
                           (which "single-file"))
         ;; https://github.com/gildas-lormeau/single-file-cli
         ;; to install npm install -g "single-file-cli"
         args (filter some? [single-file-bin (format "--browser-executable-path=%s" chrome-bin)
                             (format "--filename-template={page-title}-{time-locale}.html")
                             (when (= chrome-bin "google-chrome")
                               "--browser-args=[\"--no-sandbox\"]")
                             url out-path])
         _    (assert (not (str/blank? chrome-bin)) "Could not find `CHROME_BINARY` in your system")
         _    (assert (not (str/blank? single-file-bin)) "Could not find `single-file` in your system")
         out  (apply sh args)]
     (log/info ::single-file out)
     out)))
