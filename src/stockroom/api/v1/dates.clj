(ns stockroom.api.v1.dates
  (:require [stockroom.api.util.response :refer [when-status] :as util-response]
            [stockroom.api.v1.format :refer [parse-date-from-web]]))

(defn parse-dates [{:keys [start end]} start-error end-error]
  (let [errors  []
        results {}
        [results errors] (if start
                           (if-let [date (parse-date-from-web start)]
                             [(assoc results :start date) errors]
                             [results (conj errors start-error)])
                           [results errors])
        [results errors] (if end
                           (if-let [date (parse-date-from-web end)]
                             [(assoc results :end date) errors]
                             [results (conj errors end-error)])
                           [results errors])]
    [results errors]))
