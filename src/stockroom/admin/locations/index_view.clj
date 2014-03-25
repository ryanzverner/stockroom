(ns stockroom.admin.locations.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml render-locations-index-view [view-data]
  [:h1.action-title "Locations"]
  [:a.action {:href (:new-location-url view-data)} "+ Add New Location"]
  [:table
   [:thead
    [:tr
     [:td "Name"]]]
   [:tbody
    (for [{:keys [name name]} (:locations view-data)]
      [:tr
       [:td (h name)]])]])