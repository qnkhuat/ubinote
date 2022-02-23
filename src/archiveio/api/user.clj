(ns archiveio.api.user
  (:require [compojure.core :refer [context defroutes POST GET]]
            [compojure.coercions :refer [as-int]]
            [archiveio.model.user :refer [User]]
            [archiveio.model.common.schemas :as schemas]
            [slingshot.slingshot :refer [try+]]
            [schema.core :as s]
            [toucan.db :as db]))

(def ^:private validate-add-user
  "Schema for adding a user"
  (s/validator
    {:email                (s/maybe schemas/EmailAddress)
     :first-name           (s/maybe schemas/NonBlankString)
     :last-name            (s/maybe schemas/NonBlankString)
     s/Keyword                              s/Any}))

(defn add-user
  [{:keys [params] :as _req}])

(defroutes routes
  (POST "/" [] ))
