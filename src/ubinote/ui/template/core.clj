(ns ubinote.ui.template.core
  (:require
   [hiccup.page :as h.page]
   [hiccup2.core :as h]
   [ring.util.response :as response]))

(defn hiccup->html-response
  "Render a hiccup html response."
  [resp]
  (-> (h/html {} (h.page/doctype :html5) resp)
      str
      response/response
      (response/content-type "text/html")))

(defn ^:private bare-html
  [children {:keys [htmx? bootstrap?]}]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (when bootstrap?
      [:link {:crossorigin "anonymous"
              :rel         "stylesheet"
              :integrity   "sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
              :href        "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"}])
    (when htmx?
      [:script {:src "https://unpkg.com/htmx.org@1.9.10"}]
      #_[:script {:src "https://unpkg.com/hyperscript.org@0.9.12"}])]
   [:body
    children
    (when bootstrap?
      [:script {:src         "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                :integrity   "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                :crossorigin "anonymous"}])]])

(defn html-response
  "Given a children, render it as a whole page"
  [children & options]
  (hiccup->html-response (bare-html children options)))

(defn layout
  [children]
  (html-response children {:htmx? true :bootstrap? true}))
