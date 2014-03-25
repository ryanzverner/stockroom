(ns stockroom.admin.people.form-view
  (:require [hiccup.def :refer [defhtml]]
            [stockroom.admin.util.view-helper :refer [render-errors]]))

(defhtml
  render-person-form-inputs [{:keys [errors params]}]
  [:div
   (render-errors errors :first-name)
   [:label "First Name*"]
   [:input {:type :text :name "first-name" :value (:first-name params)}]]

  [:div
   (render-errors errors :first-name)
   [:label "Last Name*"]
   [:input {:type :text :name "last-name" :value (:last-name params)}]]

  [:div
   (render-errors errors :email)
   [:label "Email*"]
   [:input {:type :text :name "email" :value (:email params)}]]

  [:div
   [:input {:type :submit :value "Submit"}]])
