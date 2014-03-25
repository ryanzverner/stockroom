(ns stockroom.admin.employment.edit-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml
  render-edit-employment-view [view-data form-body]
  [:div
   [:h2 "Edit Employment"]
   (form-to [:put (:update-employment-url view-data)]
            form-body)

  [:br ]

  [:h2 "Location assignments"]
  [:a {:href (:new-location-membership-url view-data)} "New Location Assignment"]

  [:br ]

  [:table
   [:thead
    [:tr
     [:td "Location"]
     [:td "Start date"]
     [:td ""]]]
   [:tbody
    (for [{:keys [location, start, location-membership-id]} (:location-memberships view-data)]
      [:tr
       [:td location]
       [:td start]
       [:td (form-to [:delete (:delete-location-membership-url view-data)]
                     [:input {:type :hidden :name "location-membership-id" :value location-membership-id}]
                     [:input.caution {:type :submit :value "Remove"}])]])]]])