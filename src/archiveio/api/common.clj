(ns archiveio.api.common)

(defn assert-400
  "Throw ex-info which will result in 400 response if assertion fails"
  [x field message]
  (when-not x
    (throw (ex-info message
                    {:type  ::bad-parameter
                     :field field}))))

(defn assert-404
  "Throw ex-info which will result in 404 response if assertion fails"
  [x message]
  (when-not x
    (throw (ex-info message {:type ::not-found}))))

