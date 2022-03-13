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

(def env (read-env))

;; TODO: validate config with spec
(def default
  {:un-db-type          "postgres" ; #{h2, postgres}
   :un-db-name          "ubinote"
   :un-db-host          "localhost" ; postgres
   :un-db-port          "5432"      ; postgres
   :un-port             "8000"
   :un-max-session-age "20160" ; session length in minutes (14 days)
   ;; root to store and serve archived files
   :un-root    ".ubinote"})

(defn config-str
  "Retrieve value for a single configuration key
  These values could be configured from:
  1.  environment variables (ex: un_DB_TYPE -> :un-db-type)
  2.  jvm options (ex: -Dun.db.type -> :un-db-type)
  3.  hard coded `app-defaults`"
  [k]
  (let [k       (keyword k)
        env-val (k env)]
    (or (when-not (string/blank? env-val) env-val)
        (k default))))

(defn config-kw [k]
  (some-> k
          config-str
          keyword))

(defn config-int [k]
  (some-> k
          config-str
          Integer/parseInt))

(defn config-bool [k]
  (some-> k
          config-str
          Boolean/parseBoolean))
