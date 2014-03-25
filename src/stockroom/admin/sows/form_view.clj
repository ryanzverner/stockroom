(ns stockroom.admin.sows.form-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [select-options]]
            [stockroom.admin.util.view-helper :refer [render-errors
                                                      sanitize-select-options]]))

(defhtml
  render-sow-form-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]

   [:div
     (render-errors errors :sow-id)

    [:div
     (render-errors errors :start)
     [:label "Start Date*"]
     [:input {:type :date
              :name :start
              :value (:start params)}]]

    [:div
     [:label "End Date"]
     [:input {:type :date
              :name :end
              :value (:end params)}]]

    [:div
     (render-errors errors :hourly-rate)
     [:label "Hourly Rate*"]
     [:input {:type :text
              :name :hourly-rate
              :value (:hourly-rate params)}]]

    [:div
     [:label "Currency code"]
     [:select {:name "currency-code"}
       (-> (:currency-code-options view-data)
         sanitize-select-options
         (select-options (:currency-code params)))]]

    [:div
     [:label "URL"]
     [:input {:type :text
              :name :url
              :value (:url params)}]]

    [:div
     (render-errors errors :signed-date)
     [:label "Date Signed"]
     [:input {:type :date
              :name :signed-date
              :value (:signed-date params)}]]

    [:div
     (render-errors errors :projects)
     [:label "Projects (select at least one)"]
     (for [project (:projects view-data)]
      [:div
        [:input {:type :checkbox
                 :name :projects
                 :checked (:checked project)
                 :value (:id project)}
                 (:name project)]])]

    [:div
     [:input {:type :submit :value "Submit"}]]]))