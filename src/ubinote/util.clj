(ns ubinote.util
  (:require
   [potemkin :as p]
   [ubinote.util.random :as util.random]
   [ubinote.util.time :as util.time]))

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(p/import-vars
 [util.random
  random-name
  random-email]

 [util.time
  format-seconds
  format-milliseconds
  format-microseconds
  ->millis-from-epoch])
