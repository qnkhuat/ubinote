(ns ubinote.models.session
  (:require
    [methodical.core :as m]
    [toucan2.core :as tc])
  (:import java.util.UUID))

(m/defmethod tc/table-name :m/session
  [_model]
  "session")

(derive :m/session :hooks/created-at-timestamped)

(tc/define-before-insert :m/session
  [session]
  (merge session
         {:id (.toString (UUID/randomUUID))}))

(tc/define-before-update :m/session
  [_session]
  (throw (ex-info "Not allowed to update session." {})))
