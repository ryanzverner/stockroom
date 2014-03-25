(ns stockroom.admin.skills.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml render-skills-index-view [view-data]
  [:h2 "Skills"]
  [:a {:href (:new-skill-url view-data)} "New Skill"]
  [:table
   [:thead
    [:tr
     [:td "Name"]
     [:td ]]]
   [:tbody
    (for [skill (:skills view-data)]
      [:tr
       [:td [:a {:href (:show-url skill)} (h (:name skill))]]
       [:td [:a {:href (:edit-url skill)} "Edit Skill"]]])
    ]]
  )
