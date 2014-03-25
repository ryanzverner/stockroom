(ns stockroom.admin.apprenticeships.form
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to select-options]]
            [clojure.string :refer [capitalize]]
            [stockroom.v1.validations :refer [valid-apprenticeship-skill-levels]]
            [stockroom.admin.util.view-helper :refer [render-errors
                                                      sanitize-select-options]]))

(def apprenticeship-skill-level-options
  (cons ["Select a Type" ""]
        (map (fn [skill-level] [(capitalize skill-level) skill-level])
             valid-apprenticeship-skill-levels)))

(defhtml render-apprenticeship-form-view [view-data]
  (let [params (:params view-data)]
    (list
      [:div
       [:label "Apprentice*"]
       [:select {:name "apprentice-id"}
        (-> (:person-options view-data)
            sanitize-select-options
            (select-options (:person-id params)))]]

      [:div
       [:label "Skill Level"]
       [:select {:name "skill-level"}
        (select-options apprenticeship-skill-level-options)]]

      [:div
       [:label "Start Date*"]
       [:input {:type :date
                :name "apprenticeship-start-date"
                :value (:start params)}]]

      [:div
       [:label "End Date*"]
       [:input {:type :date
                :name "apprenticeship-end-date"
                :value (:end params)}]]

      [:div
       [:label "Mentor*"]
       [:select {:name "mentor-id"}
        (-> (:person-options view-data)
            sanitize-select-options
            (select-options (:person-id params)))]]

      [:div
       [:label "Mentor Start Date*"]
       [:input {:type :date
                :name "mentorship-start-date"
                :value (:start params)}]]

      [:div
       [:label "Mentor End Date*"]
       [:input {:type :date
                :name "mentorship-end-date"
                :value (:end params)}]]

      [:div
       [:input {:type :submit :value "Submit"}]])))
