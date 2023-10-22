(ns ubinote.config
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [toucan2.core :as tc]))

(defn- keywordize [s]
  (-> (str/lower-case s)
      (str/replace "_" "-")
      (str/replace "." "-")
      (keyword)))

(defn- read-system-props []
  (->> (System/getProperties)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-env []
  (apply merge
         (read-system-props)
         (read-system-env)))


;; TODO: validate config with spec
(def default
  {:un-run-mode          "prod"
   :un-db-connection-url (format "jdbc:h2:file:%s" (.getAbsolutePath (io/file "ubinote")))
   :un-port              "8000"
   :un-max-session-age   "20160"      ;; session length in minutes (14 days)
   :un-single-file-bin   nil          ;; path to single-file binary
   :un-root              ".ubinote"}) ;; root to store and serve archived files

;; MB_DB_CONNECTION_URI=jdbc:postgresql://localhost:5432/metabase-test?user=postgres

(def env (merge default (read-env)))

(defn config-str
  "Retrieve value for a single configuration key
  These values could be configured from:
  1. environment variables (ex: un_DB_TYPE -> :un-db-type)
  2. jvm options (ex: -Dun.db.type -> :un-db-type)
  3. hard coded [[defaults]]"
  [k]
  ((keyword (format "un-%s" (name k))) env))

(defn config-kw
  "Retrieve a config and returns the value as a keyword.
  (config-kw :run-mode)
  ;; => :prod"
  [k]
  (some-> k
          config-str
          keyword))

(defn config-int
  "Retrieve a config and returns the value as a integer.
  (config-kw :port)
  ;; => 8000"
  [k]
  (some-> k
          config-str
          Integer/parseInt))

(defn config-bool
  "Retrieve a config and returns the value as a boolean."
  [k]
  (some-> k
          config-str
          Boolean/parseBoolean))

;; ------------------------------- Default config ----------------------------- ;;
(def run-mode
  (config-kw :run-mode))

(def is-dev?
  (= run-mode :dev))

(def is-prod?
  (= run-mode :prod))

;; TODO: cache this
(defn setup?
  "Did the app set up successfully?
  Meaning we created an user."
  []
  (some? (tc/select-one :m/user)))
