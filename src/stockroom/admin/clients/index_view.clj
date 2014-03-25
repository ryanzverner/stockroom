(ns stockroom.admin.clients.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-clients-index-view [view-data]
  [:h2 "Clients"]
  [:a {:href (:new-client-url view-data)} "New Client"]
  [:table
   [:thead
    [:tr
     [:td "Name"]
     [:td ]
     [:td ]]]
   [:tbody
    (for [client (:clients view-data)]
      [:tr
       [:td [:a {:href (:show-url client)} (h (:name client))]]
       [:td [:a {:href (:edit-url client)} "Edit Client"]]
       (if (not (nil? (:delete-url client)))
         [:td (form-to [:delete (:delete-url client)]
                       [:input {:type :hidden :name "client-id" :value (:id client)}]
                       [:input.caution {:type :submit :value "Remove"}])]
         [:td ""])])]]
  )