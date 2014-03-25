(ns stockroom.admin.skills.show-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]))

(defhtml render-show-skill-view [view-data]
  [:h1 (h (:skill-name view-data))])
