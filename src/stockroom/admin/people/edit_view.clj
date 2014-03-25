(ns stockroom.admin.people.edit-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.people.form-view :refer [render-person-form-inputs]]))

(defhtml
  render-edit-person-view [{:keys [update-person-url] :as view-data}]
  [:div
   [:h2 "Edit Person"]
   (form-to [:put update-person-url]
            (render-person-form-inputs view-data))
   ])
