(ns stockroom.admin.projects.edit-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.util.view-helper :refer [render-errors]]))

(defhtml render-edit-project-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]

    [:div
     [:h2 "Edit " (h (:project-name view-data))]
     (form-to [:put (:update-project-url view-data)]
              (when-let [name-errors (:name errors)]
                [:ul
                 (for [error name-errors]
                   [:li error])])
              [:label "Name"]
              [:input {:type :text :name :name :value (:name params)}]

              [:div
              (render-errors errors :skills)
              [:label "Skills"]
              (for [skill (:skills view-data)]
                [:div
                  [:input {:type :checkbox
                           :name :skills
                           :checked (:checked skill)
                           :value (:id skill)}
                          (:name skill)]])]

              [:input {:type :submit :value "Submit"}])]))
