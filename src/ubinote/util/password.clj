(ns ubinote.util.password
  (:require
   [ubinote.util :as u])
  (:import
   (org.mindrot.jbcrypt BCrypt)))

;; copied from cemerick.friend.credentials EPL v1.0 license
(defn hash-bcrypt
  "Hashes a given plaintext password using bcrypt and an optional
  :work-factor (defaults to 10 as of this writing).  Should be used to hash
  passwords included in stored user credentials that are to be later verified
  using `bcrypt-credential-fn`."
  [password & {:keys [work-factor]}]
  (BCrypt/hashpw password (if work-factor
                            (BCrypt/gensalt work-factor)
                            (BCrypt/gensalt))))

(defn bcrypt-verify
  "Returns true if the plaintext [password] corresponds to [hash],
  the result of previously hashing that password."
  [password hash]
  (BCrypt/checkpw password hash))

(defn verify-password
  "Verify if a given unhashed password + salt matches the supplied hashed-password. Returns `true` if matched, `false`
  otherwise."
  ^Boolean [password salt hashed-password]
  ;; we wrap the friend/bcrypt-verify with this function specifically to avoid unintended exceptions getting out
  (boolean (u/ignore-exceptions
            (bcrypt-verify (str salt password) hashed-password))))
