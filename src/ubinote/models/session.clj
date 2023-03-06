(ns ubinote.models.session
  (:require
    [methodical.core :as m]
    [toucan.models :as models]
    [toucan2.core :as tc])
  (:import java.util.UUID))

(m/defmethod tc/table-name :m/session
  [_model]
  "session")

(models/defmodel Session :session)

(defn- pre-insert
  [session]
  (merge session
         {:id (.toString (UUID/randomUUID))}))

(defn- pre-update
  [_session]
  (throw (ex-info "Not allowed to update session." {})))

(tc/define-before-insert :m/session
  [session]
  (merge session
         {:id (.toString (UUID/randomUUID))}))

(tc/define-before-update :m/session
  [_session]
  (throw (ex-info "Not allowed to update session." {})))

(extend (class Session)
  models/IModel
  (merge models/IModelDefaults
         {:properties (constantly {:created-at-timestamped? true})
          :pre-insert pre-insert
          :pre-update pre-update}))
