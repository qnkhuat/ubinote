(ns archiveio.api.archive
  (:require [compojure.core :refer [defroutes POST]]
            [taoensso.timbre :as log]
            [ring.util.response :refer [response]]))

(defn add-archive
  [{:keys [params] :as request}]
  (response params)
  )

(defroutes routes
  (POST "/" [] add-archive))
