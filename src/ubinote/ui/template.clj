(ns ubinote.ui.template
  (:require
   [clojure.string :as str]
   [hiccup.page :as h.page]
   [hiccup2.core :as h]
   [ring.util.response :as response]
   [ubinote.api.util :as api.u]
   [ubinote.config :as cfg]))

(defn- multi-html-response?
  "Is x of this form
  [[:p 1]
   [:p 2]]"
  [x]
  (and (coll? x)
       (pos? (count x))
       (coll? (first x))))

(def ^:dynamic *return-raw-hiccup*
  "Used for dev purposes so we can return http responses with raw hiccup.

     (dev/user-http-request 1 :get \"/comment/1\")"
  false)

(defn render-hiccup-fragment
  "Render a hiccup fragment to html"
  ([form]
   (render-hiccup-fragment form nil))
  ([form doctype]
   (-> (if *return-raw-hiccup*
         form
         (if (multi-html-response? form)
           (str/join (map #(h/html {} doctype %) form))
           (str (h/html {} doctype form))))
       response/response
       (response/content-type "text/html"))))

(defn with-nav-bar
  [children]
  [:div
   [:nav {:class "navbar navbar-expand-lg bg-dark"}
    [:div {:class "container-fluid"}
     [:a {:class "navbar-brand text-light"
          :href  "/"}
      "Ubinote"]
     (when (and (cfg/setup?) (some? api.u/*current-user-id*))
       [:div {:class "d-flex"}
        [:a {:class "text-light text-decoration-none me-2"
             :href  "/comments"} "Comments"]
        [:a {:class "text-light text-decoration-none"
             :href  "/user"} "User"]])]]
   children])

(def ^:private bootstrap-css
  [:link {:crossorigin "anonymous"
          :rel         "stylesheet"
          :integrity   "sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
          :href        (if false #_cfg/is-dev? "/static/bootstrap.min.css" "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css")}])

(def ^:private bootstrap-icon-css
  [:link {:rel  "stylesheet"
          :href "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"}])

(def ^:private htmx-js
  [:script {:src (if cfg/is-dev? "/static/htmx_1.9.10.js" "https://unpkg.com/htmx.org@1.9.10")}])

(def ^:private bootstrap-js
  [:script {:src         (if false #_cfg/is-dev?
                           "/static/bootstrap.bundle.min.js"
                           "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js")
            :integrity   "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            :crossorigin "anonymous"}])

(def ^:private alpine-js [:script {:src "//unpkg.com/alpinejs" :defer true}])

(defn ^:private bare-html
  [children & {:keys [scripts? navbar?]
               :or   {scripts?   true
                      navbar?    true}
               :as _options}]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:rel  "stylesheet"
            :href "/static/global.css"}]
    (when scripts? bootstrap-css)
    (when scripts? bootstrap-icon-css)
    (when cfg/is-dev? [:script {:src "/static/termlog.js"}])
    (when scripts? htmx-js)
    [:script {:src "/static/app.js"}]]

   [:body {:hx-boosted "true"}
    (cond-> children
      navbar?
      with-nav-bar)
    alpine-js
    #_(when scripts?
        [:script {:src "https://unpkg.com/hyperscript.org@0.9.12"}])
    (when scripts?
      bootstrap-js)]])

(defn html-response
  "Given a children, render it as a whole page.

  By defaul the rendered page will have htmx, bootstrap and a navbar."
  [children & options]
  (render-hiccup-fragment (apply bare-html children options) (h.page/doctype :html5)))
