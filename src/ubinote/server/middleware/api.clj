(ns ubinote.server.middleware.api
  (:require [clojure.tools.logging :as log]))

(defn resp->log-level
  [{:keys [status]}]
  (cond
   (< 400 status) :info
   :else :info))

(defn format-nanoseconds
  "Format a time interval in nanoseconds to something more readable. (¬µs/ms/etc.)"
  ^String [nanoseconds]
  ;; The basic idea is to take `n` and see if it's greater than the divisior. If it is, we'll print it out as that
  ;; unit. If more, we'll divide by the divisor and recur, trying each successively larger unit in turn. e.g.
  ;;
  ;; (format-nanoseconds 500)    ; -> "500 ns"
  ;; (format-nanoseconds 500000) ; -> "500 ¬µs"
  (loop [n nanoseconds, [[unit divisor] & more] [[:ns 1000] [:¬µs 1000] [:ms 1000] [:s 60] [:mins 60] [:hours 24]
                                                 [:days 7] [:weeks (/ 365.25 7)] [:years Double/POSITIVE_INFINITY]]]
    (if (and (> n divisor)
             (seq more))
      (recur (/ n divisor) more)
      (format "%.1f %s" (double n) (name unit)))))

(defn wrap-request-logger
  [handler]
  (fn [req]
    (let [start-time (System/nanoTime)
          resp       (handler req)
          end-time   (System/nanoTime)]
      (log/log (resp->log-level resp)
               (format "%s %s %d %s"
                       (:request-method req)
                       (:uri req)
                       (:status resp)
                       (format-nanoseconds (- end-time start-time))))
      resp)))

