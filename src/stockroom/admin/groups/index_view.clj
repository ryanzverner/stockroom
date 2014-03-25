(ns stockroom.admin.groups.index-view
  (:require [hiccup.def :refer [defhtml]]))

(defhtml render-group-index-view [view-data]
  [:div
   [:h1.action-title "Groups"]
   [:a.action {:href (:new-group-url view-data)} "+ Add New Group"]
   [:table
    [:thead
     [:tr
      [:td "Name"]]]
    [:tbody
     (for [{:keys [url name]} (:groups view-data)]
       [:tr
        [:td [:a {:href url} name]]])]]
   ]
  )
