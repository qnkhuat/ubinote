(ns dev.h2-shell
  (:require
   [ubinote.server.db :as db]))

(defn shell
  "Open an H2 shell with `clojure -X:h2`."
  [& _args]
  ;; Force the DB to use h2 regardless of what's actually in the env vars for Java properties
  (require 'ubinote.config :reload)
  (org.h2.tools.Shell/main
   (into-array
    String
    ["-url" (let [url (.getJdbcUrl (:datasource db/*application-db*))]
              (println "Connecting to database at URL" url)
              url)])))
