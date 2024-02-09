(ns ubinote.ui.template.core)

(defn layout
  [children]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:crossorigin "anonymous"
            :href        "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"}]
    [:script {:src "https://unpkg.com/htmx.org@1.9.10"}]]
   [:body
    children
    [:script {:src         "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
              :integrity   "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
              :crossorigin "anonymous"}]]])

(def unauthorized
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
   [:body
    [:div {:class "container"}
     [:div {:class "row"}
      [:div {:class "col"}
       [:h1 "Unauthorized"]
       [:p "You are not authorized to view this page."]
       [:a {:href "/login"} "Login"]]]]]])
