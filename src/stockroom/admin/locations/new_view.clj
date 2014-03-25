(ns stockroom.admin.locations.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.locations.form-view :refer [render-location-form-inputs]]))

(defhtml
  render-new-location-view [{:keys [create-location-url] :as view-data}]
  [:div
   [:h2 "New Location"]
   (form-to [:post create-location-url]
            (render-location-form-inputs view-data))])