(ns ubinote.models
  (:require
   [ubinote.models.annotation :as annotation]
   [ubinote.models.comment :as comment]
   [ubinote.models.migration :as migration]
   [ubinote.models.page :as page]
   [ubinote.models.session :as session]
   [ubinote.models.user :as user]))

;; mostly importing things here so these are loaded on startup

(comment
 annotation/keep-me
 comment/keep-me
 migration/keep-me
 page/keep-me
 session/keep-me
 user/keep-me)
