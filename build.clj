(ns build
  (:require
    [clojure.java.shell :refer [sh]]
    [clojure.tools.build.api :as b]))

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/ubinote.jar")

(defn clean [_]
  (b/delete {:path "target/"})
  ;; clean for frontend
  (b/delete {:path "node_modules/"})
  (b/delete {:path "resources/frontend/build/"})
  (ignore-exceptions
    (sh "rm" "-rf" "resources/frontend/build_*")))

(defn- build-frontend
  []
  (println "Building frontend")
  (sh "npm" "install")
  (sh "npm" "run" "build"))

(defn- build-backend
  []
  (println "Building backend")
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
  (println "Start to build")
  (clean nil)
  (build-frontend)
  (build-backend)
  (println "Built successfully!"))
