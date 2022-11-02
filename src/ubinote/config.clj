(ns ubinote.config
  (:require [clojure.string :as string]))

(defn- keywordize [s]
  (-> (string/lower-case s)
      (string/replace "_" "-")
      (string/replace "." "-")
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
 {:un-run-mode         "prod"
  :un-db-type          "postgres" ; #{h2, postgres}
  :un-db-name          "ubinote"
  :un-db-host          "localhost" ; postgres
  :un-db-port          "5432"      ; postgres
  :un-port             "8000"
  :un-max-session-age  "20160" ; session length in minutes (14 days)
  ;; root to store and serve archived files
  :un-root             ".ubinote"})

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
  (config-kw :db-port)
  ;; => 5432"
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
