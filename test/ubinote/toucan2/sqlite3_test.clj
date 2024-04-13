(ns ubinote.toucan2.sqlite3-test
  (:require
   [clojure.test :refer :all]
   [toucan2.core :as tc]
   [ubinote.util :as u]))

(deftest insert-returning-instance-test
  (is (= {:first_name "Ngoc"
          :last_name  "Khuat"}
         (select-keys (tc/insert-returning-instance! :m/user {:first_name "Ngoc"
                                                              :last_name  "Khuat"
                                                              :email      (u/random-email)
                                                              :password   "strongpassword"})
                      [:first_name :last_name])
         (select-keys (first (tc/insert-returning-instances! :m/user {:first_name "Ngoc"
                                                                      :last_name  "Khuat"
                                                                      :email      (u/random-email)
                                                                      :password   "strongpassword"}))
                      [:first_name :last_name]))))

(deftest insert-returning-pk-test
  (is (every? pos-int?
              [(tc/insert-returning-pk! :m/user {:first_name "Ngoc"
                                                 :last_name  "Khuat"
                                                 :email      (u/random-email)
                                                 :password   "strongpassword"})
               (first (tc/insert-returning-pks! :m/user {:first_name "Ngoc"
                                                         :last_name  "Khuat"
                                                         :email      (u/random-email)
                                                         :password   "strongpassword"}))])))

(deftest update-returning-pks-test
  (let [user-id (tc/insert-returning-pk! :m/user {:first_name "Ngoc"
                                                  :last_name  "Khuat"
                                                  :email      (u/random-email)
                                                  :password   "strongpassword"})]
    (is (pos-int? (first (tc/update-returning-pks! :m/user user-id {:first_name (u/random-name)}))))))
