(ns ubinote.models.common.schema
  (:require
   [clojure.string :as str]
   [malli.core :as mc]))

;; ------------------------------- Schemas -------------------------------

(def NonBlankString
  "String with at least one char."
  (mc/schema [:string {:min 1}]))

(def ^:private username-regex
  "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*")

(def Username
  "Schema for username and it's require to be at least 3 chars."
  (mc/schema
   [:and
    :string
    [:fn (fn [s]
           (and (>= (count s) 3)
                (re-matches (re-pattern username-regex) s)))]]))


(def Password
  "Schema for password and it's require to be at least 8 chars"
  (mc/schema [:string {:min 8}]))

(def IsoDateTimeMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateTimeNoMs
  "time in YYYY-MM-DDTmm:hh:ss.SSSSSSZ format"
  #"^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d([+-][0-2]\d:[0-5]\d|Z)$")

(def IsoDateNoTime
  "time in YYYY-MM-DD format"
  #"^\d{4}-[01]\d-[0-3]\d$")

(def IntegerGreaterThanZero
  (mc/schema
   [:int {:min 1}]))

(def ^:private email-regex-domain "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(def ^:private email-regex (re-pattern (str username-regex email-regex-domain)))

(def EmailAddress
  "schema for email address"
  (mc/schema
   [:and
    :string
    [:fn (fn [s] (and (seq s)
                      (re-matches email-regex (str/lower-case s))))]]))

;; https://urlregex.com
(def url-regex #"^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")

(def URL
  [:and
   :string
   [:re url-regex]])
