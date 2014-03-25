(ns stockroom.admin.users.show-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml render-show-user-view [view-data]
  [:h2 (h (:name view-data)) " belongs to the groups:" ]
  [:table
   [:thead
    [:tr
     [:td "Name"]
     [:td ""]]]
   [:tbody
    (for [{:keys [remove-url name remove-params]} (:groups view-data)]
      [:tr
       [:td (h name)]
       [:td (form-to [:delete remove-url]
                     (for [{:keys [name value]} remove-params]
                       [:input {:type :hidden :name name :value value}])
                       [:input.caution {:type :submit :value "Remove"}])]])
    ]])
