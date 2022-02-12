(ns archiveio.util.b64
  (:import java.util.Base64))

(defn encode
  "encode a string to b64 string"
  [s]
  (.encodeToString (Base64/getEncoder) (.getBytes s)))

(defn decode [to-decode]
  "decode b64 string to a string"
  (String. (.decode (Base64/getDecoder) to-decode)))
