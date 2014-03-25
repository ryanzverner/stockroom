(ns stockroom.api.v1.format
  (:import (java.text SimpleDateFormat)
           (java.util TimeZone)))

(def utc (TimeZone/getTimeZone "UTC"))

(def date-format (doto
                   (SimpleDateFormat. "yyyy-MM-dd")
                   (.setTimeZone utc)))

(defn format-date-for-web [date]
  (.format date-format date))

(defn maybe-format-date [map key]
  (if-let [value (key map)]
    (assoc map key (format-date-for-web value))
    map))

(defn parse-date-from-web [str]
  (try
    (.parse date-format str)
    (catch Exception e)))
