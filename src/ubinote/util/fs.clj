(ns ubinote.util.fs
  (:require
   [clojure.java.io :as io])
  (:import
   java.nio.file.Files))

(defn env-path [] (System/getenv "PATH"))
(def path-separator (System/getProperty "path.separator"))

(defn executable? [p]
  (-> p
      (io/file)
      (.toPath)
      (Files/isExecutable)))

(defn dir? [p]
  (-> p
      (io/file)
      (.isDirectory)))

(defn exists? [p]
  (-> p
      (io/file)
      (.exists)))

(defn absolute? [p]
  (-> p
      (io/file)
      (.isAbsolute)))

(defn path-join [& args]
  (->> args
       (apply io/file)
       str))

(defn absolute
  [p]
  (-> p
      (io/file)
      (.getAbsolutePath)))

(defn make-dirs
  "Recursively make dir, will return true if it's created, false if it's already created"
  [p]
  (-> p
      (io/file)
      (.mkdirs)))

(defn find-in
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
