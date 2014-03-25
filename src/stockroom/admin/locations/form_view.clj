(ns stockroom.admin.locations.form-view
  (:require [hiccup.def :refer [defhtml]]
            [stockroom.admin.util.view-helper :refer [render-errors]]))

(defhtml
  render-location-form-inputs [{:keys [errors params]}]
  [:div
   (render-errors errors :name)
   [:label "Name"]
   [:input {:type :text :name "name" :value (:name params)}]]

  [:div
   [:input {:type :submit :value "Submit"}]])
