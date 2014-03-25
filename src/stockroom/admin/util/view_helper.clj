(ns stockroom.admin.util.view-helper
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]])
  (:import (java.text SimpleDateFormat)
           (java.util TimeZone)))

(def utc (TimeZone/getTimeZone "UTC"))

(def month-day-year-format (doto
                             (SimpleDateFormat. "MM/dd/yyyy")
                             (.setTimeZone utc)))

(def date-input-format (doto
                         (SimpleDateFormat. "yyyy-MM-dd")
                         (.setTimeZone utc)))

(defn month-day-year [date]
  (when date
    (.format month-day-year-format date)))

(defn date-for-input [date]
  (when date
    (.format date-input-format date)))

(defn date-from-input [s]
  (when s
    (try
      (.parse date-input-format s)
      (catch Exception e))))

(defn first-last-name [first-name last-name]
  (str first-name " " last-name))

(defhtml render-errors [errors field]
         (when-let [field-errors (field errors)]
           [:ul
            (for [error field-errors]
              [:li error])]))

(defn sanitize-select-options [options]
  (map (fn [[text val]] [(h text) (str val)]) options))