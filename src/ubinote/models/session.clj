(ns ubinote.models.session
  (:require [toucan.models :as models])
  (:import java.util.UUID))

(models/defmodel Session :session)

(defn- pre-insert
  [session]
  (merge session
         {:id (.toString (UUID/randomUUID))}))

(defn- pre-update
  [session]
  (throw (ex-info "Not allowed to update session.")))


(extend (class Session)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:created-at-timestamped? true})
          :pre-insert pre-insert
          :pre-update pre-update}))
