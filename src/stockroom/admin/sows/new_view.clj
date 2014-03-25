(ns stockroom.admin.sows.new-view
  (:require [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to]]
            [stockroom.admin.sows.form-view :refer [render-sow-form-view]]))

(defhtml
  render-new-sow-view [{:keys [create-sow-url] :as view-data}]
  [:div
   [:h2 "New SOW"]
   (form-to [:post create-sow-url]
            (render-sow-form-view view-data))])
