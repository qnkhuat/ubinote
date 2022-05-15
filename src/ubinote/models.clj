(ns ubinote.models
  (:require [ubinote.models.page :as page]
            [ubinote.models.user :as user]
            [ubinote.models.annotation :as annotation]
            [ubinote.models.comment :as m-comment]
            [potemkin :as p]))

(comment
 page/keep-me
 user/keep-me
 annotation/keep-me
 m-comment/keep-me)

(p/import-vars
 [page Page]
 [user User]
 [annotation Annotation]
 [m-comment Comment])
