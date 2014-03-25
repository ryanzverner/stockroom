(ns stockroom.admin.apprenticeships.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml render-apprenticeships-index-view [view-data]
  [:h2 "Apprenticeships"]
  [:a {:href (:new-apprenticeship-url view-data)} "New Apprenticeship"]
  [:p view-data]
  [:table
   [:thead
    [:tr
     [:td "Apprentice"]
     [:td "Mentor"]
     [:td "Skill Level"]
     [:td "Start"]
     [:td "End"]]]
   [:tbody
    (for [{:keys [person-name mentors skill-level start end]} (:apprenticeships view-data)]
      [:tr
       [:td person-name]
       [:td mentors]
       [:td skill-level]
       [:td start]
       [:td end]])]])
