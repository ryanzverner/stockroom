(ns stockroom.admin.location-memberships.form-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [select-options]]
            [stockroom.admin.util.view-helper :refer [render-errors
                                                      sanitize-select-options]]))

(defhtml
  render-location-membership-form-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]
    (list

      [:div
       (render-errors errors :location-id)
       [:label "Location*"]
       [:select {:name "location-id"}
        (-> (:location-options view-data)
          sanitize-select-options
          (select-options (:location-id params)))]]

      [:div
       (render-errors errors :start)
       [:label "Start Date*"]
       [:input {:type :date
                :name :start
                :value (:start params)}]]

      [:div
       [:input {:type :submit :value "Submit"}]])))
