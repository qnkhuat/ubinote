(ns ubinote.toucan2.sqlite3-test
  (:require
   [clojure.test :refer :all]
   [toucan2.core :as tc]))

(tc/insert-returning-pks! :m/user {:first_name "ngoc"
                                   :last_name  "ha"
                                   :email      "ngoc@abc.com"
                                   :password   "hahalalalele"})

(deftest toucan2-test
  (testing "Ye"
    (is (= 1 1))))
