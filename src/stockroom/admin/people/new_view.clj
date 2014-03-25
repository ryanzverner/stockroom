(ns stockroom.admin.people.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.people.form-view :refer [render-person-form-inputs]]))

(defhtml
  render-new-person-view [{:keys [create-person-url] :as view-data}]
  [:div
   [:h2 "New Person"]
   (form-to [:post create-person-url]
            (render-person-form-inputs view-data))
   ])
