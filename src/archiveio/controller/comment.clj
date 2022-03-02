(ns archiveio.controller.comment
  (:require [archiveio.model.comment :refer [Comment]]
            [toucan.db :as db]
            [schema.core :as s]))

(def NewComment
  {:user-id               s/Int
   :annotation-id         s/Int
   :content               s/Str
   })

(s/defn create
  "Detect file type and archive file"
  [comment :- NewComment]
  (db/insert! Comment comment))


;(db/insert! Comment
;            {:user-id 1
;             :annotation-id 3
;             :content "whatup"})

;(db/select Comment)
;;; => [{:id 1,
;  :user-id 1,
;  :annotation-id 4,
;  :content
;  #object[org.h2.jdbc.JdbcClob 0x25bf94c0 "clob1: 'what sup every body'"],
;  :created-at #inst "2022-02-24T19:23:02.572364000-00:00",
;  :updated-at #inst "2022-02-24T19:23:02.572364000-00:00"}
; {:id 2,
;    :user-id 1,
;      :annotation-id 4,
;        :content
;          #object[org.h2.jdbc.JdbcClob 0x6371bf47 "clob2: 'what sup every body'"],
;            :created-at #inst "2022-02-24T19:23:13.520150000-00:00",
;              :updated-at #inst "2022-02-24T19:23:13.520150000-00:00"}
; {:id 3,
;    :user-id 1,
;      :annotation-id 4,
;        :content
;          #object[org.h2.jdbc.JdbcClob 0x496005d9 "clob3: 'what sup every body'"],
;            :created-at #inst "2022-02-24T19:23:26.118346000-00:00",
;              :updated-at #inst "2022-02-24T19:23:26.118346000-00:00"}
; {:id 4,
;    :user-id 1,
;      :annotation-id 8,
;        :content
;          #object[org.h2.jdbc.JdbcClob 0x58b9d1e8 "clob4: 'what sup every body'"],
;            :created-at #inst "2022-02-27T17:50:30.178392000-00:00",
;              :updated-at #inst "2022-02-27T17:50:30.178392000-00:00"}
; {:id 5,
;    :user-id 1,
;      :annotation-id 7,
;        :content
;          #object[org.h2.jdbc.JdbcClob 0x76d82bbf "clob5: 'what sup every body'"],
;            :created-at #inst "2022-02-27T17:50:32.840644000-00:00",
;              :updated-at #inst "2022-02-27T17:50:32.840644000-00:00"}
; {:id 6,
;    :user-id 1,
;      :annotation-id 3,
;        :content #object[org.h2.jdbc.JdbcClob 0x78a994d6 "clob6: 'whatup'"],
;          :created-at #inst "2022-02-27T17:56:32.967245000-00:00",
;            :updated-at #inst "2022-02-27T17:56:32.967245000-00:00"}]
