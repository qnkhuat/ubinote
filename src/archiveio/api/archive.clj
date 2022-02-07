(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST]]))

(defroutes routes
  (POST "/" [] "sup"))
