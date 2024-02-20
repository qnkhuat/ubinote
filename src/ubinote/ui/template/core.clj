(ns ubinote.ui.template.core
  (:require
   [clojure.string :as str]
   [hiccup.page :as h.page]
   [hiccup2.core :as h]
   [ring.util.response :as response]
   [ubinote.config :as cfg]))

(defn- multi-html-response?
  "Is x of this form
  [[:p 1]
   [:p 2]]"
  [x]
  (and (coll? x)
       (pos? (count x))
       (coll? (first x))))

(defn hiccup->html-response
  "Render a hiccup html response."
  ([form]
   (hiccup->html-response form nil))
  ([resp doctype]
   (-> (if (multi-html-response? resp)
         (str/join (map #(h/html {} doctype %) resp))
         (str (h/html {} doctype resp)))
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
      [:link {:rel  "stylesheet"
              :href "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"}])
    (when cfg/is-dev?
      [:script {:src "/static/termlog.js"}])
    (when scripts?
      (if cfg/is-dev?
        [:script {:src "/static/htmx_1.9.10.js"}]
        [:script {:src "https://unpkg.com/htmx.org@1.9.10"}]))
    [:script {:src "/static/app.js"}]]
   [:body {:hx-boosted "true"}
    (cond-> children
      navbar?
      with-nav-bar)
    #_(when scripts?
        [:script {:src "//unpkg.com/alpinejs" :defer true}])
    (when scripts?
      [:script {:src "https://unpkg.com/hyperscript.org@0.9.12"}])
    #_(when scripts?
        [:script {:src "https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"}])
    (when scripts?
      [:script {:src         "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                :integrity   "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                :crossorigin "anonymous"}])]])

(defn html-response
  "Given a children, render it as a whole page.

  By defaul the rendered page will have htmx, bootstrap and a navbar."
  [children & options]
  (hiccup->html-response (apply bare-html children options) (h.page/doctype :html5)))
