(ns stockroom.admin.clients.edit-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-edit-client-view [view-data]
  (let [errors (:errors view-data)
        params (:params view-data)]

    [:div
     [:h2 (format "Edit %s" (h (:client-name view-data)))]
     (form-to [:put (:update-client-url view-data)]
              (when-let [name-errors (:name errors)]
                [:ul
                 (for [error name-errors]
                   [:li error])])
              [:label "Name"]
              [:input {:type :text :name :name :value (:name params)}]
              [:input {:type :submit :value "Submit"}])]))
