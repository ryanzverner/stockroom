(ns stockroom.admin.util.errors-view
  (:require [hiccup.def :refer [defhtml]]))

(defhtml render-errors [errors field]
         (when-let [field-errors (field errors)]
           [:ul
            (for [error field-errors]
              [:li error])]))
