(ns stockroom.admin.sows.edit-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]))

(defhtml 
  render-edit-sow-view [view-data form-body]
  [:div
   [:h2 "Edit SOW"]
   (form-to [:put (:update-sow-url view-data)]
            form-body)])