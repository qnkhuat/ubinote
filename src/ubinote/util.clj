(ns ubinote.util)

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))
