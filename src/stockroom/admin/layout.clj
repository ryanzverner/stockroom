(ns stockroom.admin.layout
  (:require [conveyor.core :refer [asset-url]]
            [hiccup.def :refer [defhtml]]
            [hiccup.page :refer [html5 include-css include-js]]
            [stockroom.admin.url-helper :as urls]))

(defhtml render-layout [body context]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:content "IE=edge,chrome=1" :http-equiv "X-UA-Compatible"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title "Admin"]

     "<!--[if lt IE 9]>"
     (include-js (asset-url "html5shiv.js"))
     "<![endif]-->"

     (include-css (asset-url "admin.css"))
     ]
    [:body
      [:header
       [:div
       [:h2 [:a {:href (urls/root-url context)} "Admin"]]
        [:section
        [:a {:href (urls/list-users-url context)} "Users"]
        [:a {:href (urls/list-groups-url context)} "Groups"]
        [:a {:href (urls/list-clients-url context)} "Clients"]
        [:a {:href (urls/list-skills-url context)} "Skills"]
        [:a {:href (urls/list-employments-url context)} "Employment"]
        [:a {:href (urls/list-locations-url context)} "Locations"]
        [:a {:href (urls/list-people-url context)} "People"]
        [:a {:href (urls/list-apprenticeships-url context)} "Apprenticeships"]
        [:a {:href (urls/logout-url context)} "Logout"]]]]
      [:div.container body]]))
