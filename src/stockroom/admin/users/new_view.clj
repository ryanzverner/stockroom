(ns stockroom.admin.users.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-new-user-view [view-data]
  (let [errors (:errors view-data)]
    [:h3 "New User"]
    (form-to [:post (:create-url-url view-data)]
             (when-let [errors (:base errors)]
               [:ul (for [error errors]
                      [:li error])])
             (when-let [uid-errors (:uid errors)]
               [:ul (for [error uid-errors]
                      [:li error])])
             [:label "Name"]
             [:input {:type :text :name :name}]

             [:label "Google User Id"]
             [:input {:type :text :name :uid}]

             [:input {:type :submit :value "Submit"}])))
