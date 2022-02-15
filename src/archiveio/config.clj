(ns archiveio.config
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
  {:aio-db-type "h2" ; #{h2, postgres}
   :aio-db-name ".archiveio"
   :aio-port    "8000"
   ;; root to store and serve archived files
   :aio-root    ".archiveio"})

(defn config-str
  "Retrieve value for a single configuration key
  These values could be configured from:
  1.  environment variables (ex: AIO_DB_TYPE -> :aio-db-type)
  2.  jvm options (ex: -Daio.db.type -> :aio-db-type)
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
