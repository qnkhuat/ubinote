(ns archiveio.model.common.schemas
  (:require [clojure.string :as str]
            [schema.core :as s]))

(def NonNilByteArray
  "byte array that is not nil"
  (s/constrained (Class/forName "[B") (fn [ba] (> (count ba) 0))))

(def NonBlankString
  "generic non blank string schema"
  (s/constrained s/Str (complement str/blank?)))

(def Password
  "Schema for password and it's require to be atleast 8 chars"
  ;; TODO - enforce some sort of complexity
  (s/constrained s/Str #(> (count %) 7)))

(def IsoDateTimeMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateTimeNoMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateNoTime
  "time in YYYY-MM-DD format"
  #"^\d{4}-[01]\d-[0-3]\d$")

(def ^:private email-regex-username "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*")
(def ^:private email-regex-domain "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(def ^:private email-regex (re-pattern (str email-regex-username email-regex-domain)))

(def EmailAddress
  "schema for email address"
  (s/constrained
    NonBlankString
    (fn [s] (and (seq s)
                 (re-matches email-regex (str/lower-case s))))))

(defn- string-starting-with-schema [prefix schema-name]
  (s/constrained s/Str #(str/starts-with? % prefix) schema-name))
