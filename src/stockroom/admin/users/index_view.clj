(ns stockroom.admin.users.index-view
  (:require [hiccup.def :refer [defhtml]]))

(defhtml render-users-index-view [view-data]
  [:h1.action-title "Users"]
  [:a.action {:href (:new-user-url view-data)} "+ Add New User"]
  [:table
   [:thead
    [:tr
     [:td "Name"]]]
   [:tbody
    (for [{:keys [url name]} (:users view-data)]
      [:tr
       [:td [:a {:href url} name]]])]])
