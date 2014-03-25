(ns stockroom.admin.projects.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-new-project-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]

    [:div
     [:h2 "New Project"]
     (form-to [:post (:create-project-url view-data)]
              (when-let [name-errors (:name errors)]
                [:ul
                 (for [error name-errors]
                   [:li error])])
              [:label "Name"]
              [:input {:type :text :name :name :value (:name params)}]
              [:input {:type :submit :value "Submit"}])]))
