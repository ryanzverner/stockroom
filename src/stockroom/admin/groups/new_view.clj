(ns stockroom.admin.groups.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-new-group-view [view-data]
  (let [errors (:errors view-data)
        create-group-url (:create-group-url view-data)]
    [:h3 "New Group"]
    (form-to [:post create-group-url]
             (when-let [errors (:base errors)]
               [:ul (for [error errors]
                      [:li error])])
             (when-let [errors (:name errors)]
               [:ul (for [error errors]
                      [:li error])])
             [:label "Group Name:"]
             [:input {:type :text :name :name}]

             [:input {:type :submit :value "Submit"}]
             )))
