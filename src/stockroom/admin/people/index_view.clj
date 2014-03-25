(ns stockroom.admin.people.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml
  render-people-index-view [view-data]
  [:h2 "People"]
  [:a {:href (:new-person-url view-data)} "New Person"]
  [:table
   [:thead
    [:tr
     [:td "First Name"]
     [:td "Last Name"]
     [:td "Email"]
     [:td ] ; Edit link
     ]]
   [:tbody
    (for [{:keys [first-name last-name email edit-url]} (:people view-data)]
      [:tr
       [:td (h first-name)]
       [:td (h last-name)]
       [:td (h email)]
       [:td [:a {:href edit-url} "Edit"]]])]]
  )
