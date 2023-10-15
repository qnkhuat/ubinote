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
  :to delete is a list of maps that has ids only in `current-items`"
  [current-items new-items]
  (let [[delete-ids create-ids update-ids] (diff (set (map :id current-items))
                                                 (set (map :id new-items)))]
    {:to-create (when (seq create-ids) (filter #(create-ids (:id %)) new-items))
     :to-delete (when (seq delete-ids) (filter #(delete-ids (:id %)) current-items))
     :to-update (when (seq update-ids) (filter #(update-ids (:id %)) new-items))}))
