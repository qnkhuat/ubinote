(ns ubinote.util
  (:require
   [clojure.data :refer [diff]]))

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(defn classify-changes
  "Given 2 lists of seq maps of changes, where each map an has an `id` key,
  return a map of 3 keys: `:to-create`, `:to-update`, `:to-delete`.

  Where:
  :to-create is a list of maps that ids in `new-items`
  :to-update is a list of maps that has ids in both `current-items` and `new-items`
  :to-delete is a list of maps that has ids only in `current-items`"
  [current-items new-items]
  (let [[delete-ids create-ids update-ids] (diff (set (map :id current-items))
                                                 (set (map :id new-items)))]
    {:to-create (when (seq create-ids) (filter #(create-ids (:id %)) new-items))
     :to-delete (when (seq delete-ids) (filter #(delete-ids (:id %)) current-items))
     :to-update (when (seq update-ids) (filter #(update-ids (:id %)) new-items))}))

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
