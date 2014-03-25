(ns stockroom.admin.employment.index-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml render-edit-url [url]
  [:a {:href url} "Edit"])

(defn- opposite-direction [current-direction]
  (if (= :asc current-direction) :desc :asc))

(def arrows {:asc "&#x2193;"
             :desc "&#x2191;"})

(def default-direction :asc)

(defhtml render-sortable-column-view [column-name column-label sorted-column sorted-direction urls]
  (let [sorted? (= column-name sorted-column)
        [direction label] (if sorted?
                            (let [direction (opposite-direction sorted-direction)
                                  arrow (get arrows direction)]
                              [direction (str (h column-label) " " arrow)])
                            [default-direction (h column-label)])]
    [:a {:href (get urls direction)} label]))

(defhtml render-employments-index-view [view-data]
  [:h2 "Employments"]
  [:a {:href (:new-employee-url view-data)} "New Employment"]
  [:table
   [:thead
    [:tr
     (for [{:keys [label-view]} (:columns view-data)]
       [:td label-view])]]
   [:tbody
    (for [employment (:employments view-data)]
      [:tr
       (for [{:keys [value-view-key]} (:columns view-data)]
         [:td (get employment value-view-key)])])]
   ])
