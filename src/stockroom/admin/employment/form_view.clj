(ns stockroom.admin.employment.form-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [select-options]]
            [stockroom.admin.util.view-helper :refer [render-errors
                                                      sanitize-select-options]]))

(defhtml
  render-employment-form-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]
    (list

      [:div
       (render-errors errors :person-id)
       [:label "Person*"]
       [:select {:name "person-id"}
        (-> (:person-options view-data)
          sanitize-select-options
          (select-options (:person-id params)))]]

      [:div
       (render-errors errors :position-id)
       [:label "Postion*"]
       [:select {:name "position-id"}
        (-> (:position-options view-data)
          sanitize-select-options
          (select-options (:position-id params)))]]

      [:div
       (render-errors errors :start)
       [:label "Start Date*"]
       [:input {:type :date
                :name :start
                :value (:start params)}]]

      [:div
       (render-errors errors :end)
       [:label "End Date"]
       [:input {:type :date
                :name :end
                :value (:end params)}]]

      (if (nil? params)
        [:div
         (render-errors errors :location-id)
         [:label "Location*"]
         [:select {:name "location-id"}
          (-> (:location-options view-data)
            sanitize-select-options
            (select-options (:location-id params)))]])

      [:div
       [:input {:type :submit :value "Submit"}]])))
