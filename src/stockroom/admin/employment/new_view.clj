(ns stockroom.admin.employment.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml
  render-new-employment-view [view-data form-body]
  [:div
   [:h2 "New Employment"]
   (form-to [:post (:create-employment-url view-data)]
            form-body)])
