(ns stockroom.admin.sows.show-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.util.view-helper :refer [month-day-year]]))

(defhtml render-show-sow-view [view-data]
  [:h1 (h (:client-name view-data))]
  [:h2 "SOW"]
  [:a.action {:href (:edit-sow-url view-data)} "Edit SOW"]
  (form-to [:delete (:delete-sow-url view-data)]
           [:input {:type :hidden :name "sow-id" :value (:id (:sow view-data))}]
           [:input.caution {:type :submit :value "Delete"}])
  [:table
   [:tbody
    [:tr
     [:td "Start"]
     [:td (month-day-year (:start (:sow view-data)))]]
    [:tr
     [:td "End"]
     [:td (month-day-year (:end (:sow view-data)))]]
    [:tr
     [:td "Hourly Rate"]
     [:td (:hourly-rate (:sow view-data))]]
    [:tr
     [:td "Currency Code"]
     [:td (:currency-code (:sow view-data))]]
    [:tr
     [:td "Signed Date"]
     [:td (month-day-year (:signed-date (:sow view-data)))]]
    [:tr
     [:td "url"]
     [:td (:url (:sow view-data))]]
    ]]
  [:br ]

  [:h2 "Projects"]
  [:table
   [:thead
    [:tr
     [:td "Name"]]]
   [:tbody
    (for [{:keys [name]} (:projects view-data)]
      [:tr
       [:td name]])]])
