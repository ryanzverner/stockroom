(ns stockroom.util.time-spec
  (:require [speclj.core :refer :all]
            [stockroom.util.time :refer [to-date-string
                                         from-date-string]]
            [chee.datetime :refer [datetime]]))

(describe "time utilities"

  (it "formats a date as yyyy-MM-dd"
     (let [date (datetime 2016 12 25)]
       (should= "2016-12-25" (to-date-string date))))

  (it "converts a yyyy-MM-dd string to a date object"
    (should= (datetime 2016 12 26) (from-date-string "2016-12-26"))))
