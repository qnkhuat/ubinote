(ns ubinote.util.time
  (:require
   [java-time.api :as t])
  (:import
   (java.time Instant LocalDate LocalDateTime LocalTime OffsetDateTime OffsetTime ZonedDateTime)))

(defn- format-with-unit [n suffix & {:keys [relative]}]
  (if relative
    (format "%d %s" (int (Math/floor n)) suffix)
    (format "%.1f %s" n suffix)))

(defn format-nanoseconds
  "Format a time interval in nanoseconds to something more readable. (µs/ms/etc.)"
  ^String [nanoseconds & options]
  ;; The basic idea is to take `n` and see if it's greater than the divisior. If it is, we'll print it out as that
  ;; unit. If more, we'll divide by the divisor and recur, trying each successively larger unit in turn. e.g.
  ;;
  ;; (format-nanoseconds 500)    ; -> "500 ns"
  ;; (format-nanoseconds 500000) ; -> "500 µs"
  ;; (format-nanoseconds 3800000000 :relative true) ; -> "1 hours"
  (loop [n nanoseconds, [[unit divisor] & more] [[:ns 1000] [:µs 1000] [:ms 1000] [:s 60] [:mins 60] [:hours 24]
                                                 [:days 7] [:weeks (/ 365.25 7)]
                                                 [:years Double/POSITIVE_INFINITY]]]
    (if (and (> n divisor)
             (seq more))
      (recur (/ n divisor) more)
      (apply format-with-unit (double n) (name unit) options))))

(defn format-microseconds
  "Format a time interval in microseconds into something more readable."
  ^String [microseconds & options]
  (apply format-nanoseconds (* 1000.0 microseconds) options))

(defn format-milliseconds
  "Format a time interval in milliseconds into something more readable."
  ^String [milliseconds & options]
  (apply format-microseconds (* 1000.0 milliseconds) options))

(defn format-seconds
  "Format a time interval in seconds into something more readable."
  ^String [seconds & options]
  (apply format-milliseconds (* 1000.0 seconds) options))

(defn ->millis-from-epoch [t]
  (when t
    (condp instance? t
      Instant        (t/to-millis-from-epoch t)
      OffsetDateTime (t/to-millis-from-epoch t)
      ZonedDateTime  (t/to-millis-from-epoch t)
      LocalDate      (->millis-from-epoch (t/offset-date-time t (t/local-time 0) (t/zone-offset 0)))
      LocalDateTime  (->millis-from-epoch (t/offset-date-time t (t/zone-offset 0)))
      LocalTime      (->millis-from-epoch (t/offset-date-time (t/local-date "1970-01-01") t (t/zone-offset 0)))
      OffsetTime     (->millis-from-epoch (t/offset-date-time (t/local-date "1970-01-01") t (t/zone-offset t))))))

(defn timestamp->ago-text
  "Returns a text from now to timestamp

    (timestamp->ago-text 2-hours-ago-timestamp) ;; => two hours ago"
  [timestamp]
  (str (format-milliseconds (- (->millis-from-epoch (t/local-date-time))
                               (->millis-from-epoch timestamp)) :relative true)
       " ago"))
