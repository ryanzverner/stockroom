(ns stockroom.admin.clients.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-new-client-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]

    [:div
     [:h2 "New Client"]
     (form-to [:post (:create-client-url view-data)]
              (when-let [name-errors (:name errors)]
                [:ul
                 (for [error name-errors]
                   [:li error])])
              [:label "Name"]
              [:input {:type :text :name :name} (:name params)]
              [:input {:type :submit :value "Submit"}])]))
