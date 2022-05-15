(ns ubinote.models.common.schemas
  (:require [clojure.string :as str]
            [schema.core :as s]))

(def NonNilByteArray
  "byte array that is not nil"
  (s/constrained (Class/forName "[B") (fn [ba] (> (count ba) 0))))

(def NonBlankString
  "generic non blank string schema"
  (s/constrained s/Str (complement str/blank?)))

(def ^:private username-regex
  "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*")

(def Username
  "Schema for username and it's require to be at least 3 chars"
  (s/constrained s/Str (fn [s]
                         (and (>= (count s) 3)
                              (re-matches (re-pattern username-regex) s)))))

(def Password
  "Schema for password and it's require to be at least 8 chars"
  (s/constrained s/Str #(>= (count %) 8)))

(def IsoDateTimeMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateTimeNoMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateNoTime
  "time in YYYY-MM-DD format"
  #"^\d{4}-[01]\d-[0-3]\d$")

(def IntegerLargerThanZero
  (s/constrained s/Int (fn [x] (> x 0))))

(def ^:private email-regex-domain "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(def ^:private email-regex (re-pattern (str username-regex email-regex-domain)))

(def EmailAddress
  "schema for email address"
  (s/constrained
    NonBlankString
    (fn [s] (and (seq s)
                 (re-matches email-regex (str/lower-case s))))))

;; https://urlregex.com
(def url-regex #"^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")

(def URL (s/constrained NonBlankString #(re-matches url-regex %)))

(defn- string-starting-with-schema [prefix schema-name]
  (s/constrained s/Str #(str/starts-with? % prefix) schema-name))
