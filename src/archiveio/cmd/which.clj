(ns archiveio.cmd.which
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import java.nio.file.Files))

(defn env-path [] (System/getenv "PATH"))
(def path-separator (System/getProperty "path.separator"))

(defn- executable? [p]
  (-> p
      (io/file)
      (.toPath)
      (Files/isExecutable)))

(defn- exists? [p]
  (-> p
      (io/file)
      (.exists)))

(defn path-join [& args]
  (->> args
       (apply io/file)
       str))

(defn absolute? [p]
  (-> p
    (io/file)
    (.isAbsolute)))

(defn- find-in
  "Finds file in provided paths."
  ([paths target] (find-in paths target exists?))
  ([paths target check]
   (loop [ps paths]
     (when-let [p (first ps)]
       (let [f (path-join p target)]
         (if (check f)
           f
           (let [more (rest ps)]
             (when (seq more)
               (recur more)))))))))

(defn which [exe]
  ;; like `which` command
  (if (absolute? exe)
    (executable? exe)
    (find-in (-> (env-path)
                 (string/split (re-pattern path-separator)))
             exe executable?)))
