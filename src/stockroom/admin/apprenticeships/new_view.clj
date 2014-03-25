(ns stockroom.admin.apprenticeships.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to select-options]]
            [stockroom.admin.apprenticeships.form :refer [render-apprenticeship-form-view]]))

(defhtml render-new-apprenticeship-view [view-data]
  [:div
   [:h2 "New Apprenticeship"]
   (form-to [:post (:create-apprenticeship-url view-data)]
            (render-apprenticeship-form-view view-data))])
