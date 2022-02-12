(ns archiveio.config
  (:require [environ.core :as env]
            [clojure.string :as string]))

;; TODO: validate config with spec
(def default
  {:archiveio-db-type "h2" ; #{h2, postgres}
   :archiveio-db-name ".archiveio"
   :archiveio-port    8000})

(defn config-str
  "Retrieve value for a single configuration key
  These values could be configured from:
  1.  environment variables (ex: MB_DB_TYPE -> :mb-db-type)
  2.  jvm options (ex: -Dmb.db.type -> :mb-db-type)
  3.  hard coded `app-defaults`"

  [k]
  (let [k       (keyword k)
        env-val (k env/env)]
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
