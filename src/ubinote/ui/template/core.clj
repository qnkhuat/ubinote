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

(defn with-nav-bar
  [children]
  [:div
   [:nav {:class "navbar navbar-expand-lg bg-dark"}
    [:div {:class "container-fluid"}
     [:a {:class "navbar-brand text-light"
          :href  "/"}
      "Ubinote"]
     [:div {:class "d-flex"}
      [:a {:class "text-light text-decoration-none"
           :href  "/user"} "User"]]]]
   children])

(defn ^:private bare-html
  [children & {:keys [scripts? navbar?]
               :or   {scripts?   true
                      navbar?    true}
               :as _options}]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]

    (when scripts?
      [:link {:crossorigin "anonymous"
              :rel         "stylesheet"
              :integrity   "sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
              :href        "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"}])
    (when scripts?
      [:script {:src "https://unpkg.com/htmx.org@1.9.10"}])
    [:script {:src "/static/app.js"}]]
   [:body
    (cond-> children
      navbar?
      with-nav-bar)
    (when scripts?
      [:script {:src         "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                :integrity   "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                :crossorigin "anonymous"}])]])

(defn html-response
  "Given a children, render it as a whole page.

  By defaul the rendered page will have htmx, bootstrap and a navbar."
  [children & options]
  (hiccup->html-response (apply bare-html children options)))

(html-response [:div 1] :scripts? false :navbar? false)
