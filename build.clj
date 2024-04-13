(ns build
  (:require
   [clojure.tools.build.api :as b]))

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/ubinote.jar")

(defn clean []
  (b/delete {:path "target/"}))

(defn- build-uberjar
  []
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis     basis
                  :src-dirs  ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis     basis
           :main      'ubinote.core})
  (println (format "Jar built: %s" uber-file)))

(defn uberjar [_]
  (println "Build start")
  (clean)
  (println "Cleaned artifacts")
  (build-uberjar)
  (println "Built successfully!"))
