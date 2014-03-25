(ns stockroom.admin.location-memberships.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml
  render-new-location-membership-view [view-data form-body]
  [:div
   [:h2 "New Location Association"]
   (form-to [:post (:create-location-membership-url view-data)]
            form-body)])
