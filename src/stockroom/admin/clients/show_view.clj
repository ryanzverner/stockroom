(ns stockroom.admin.clients.show-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-show-client-view [view-data]
  [:h1 (h (:client-name view-data))]
  [:h2 "Projects"]
  [:a.action {:href (:new-project-url view-data)} "+ Add Project"]
  (if (not-empty (:projects view-data))
    [:a.action {:href (:new-sow-url view-data)} "+ Add SOW"])
  [:table
   [:thead
    [:tr
     [:td "Name"]
     [:td "SOWs"]
     [:td ""]
     [:td ""]]]
   [:tbody
    (for [{:keys [edit-url delete-url project-name sows engagements project-id]} (:projects view-data)]
      [:tr
       [:td (h project-name)]
       [:td
       (for [{:keys [start end show-sow-url id]} sows]
         [:a {:href show-sow-url} [:p start " - " end]])]
       [:td [:a {:href edit-url} "Edit Project"]]
       (if (not (nil? delete-url))
         [:td (form-to [:delete delete-url]
                       [:input {:type :hidden :name "project-id" :value project-id}]
                       [:input.caution {:type :submit :value "Remove"}])]
         [:td ""])])
    ]])
