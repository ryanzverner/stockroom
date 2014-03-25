(ns stockroom.util.time
  (:require [chee.datetime :refer [days-ago
                                   days-from-now
                                   format-datetime
                                   parse-datetime]])
  (:import (java.util Calendar GregorianCalendar TimeZone)))

(def utc-tz (TimeZone/getTimeZone "UTC"))

(def date-format "yyyy-MM-dd")

(defn to-calendar
  "Converts a Date object into a GregorianCalendar object"
  [datetime]
  (doto (GregorianCalendar.)
    (.setTime datetime)))

(defn utc [date]
  (.getTime (doto (GregorianCalendar.)
              (.setTimeZone utc-tz)
              (.setTime date))))

(defn at-midnight
  "Sets the time portion of the Date object to 0."
  [date]
  (let [cal (doto (GregorianCalendar.)
              (.setTimeZone utc-tz)
              (.setTime date))
        new-cal (doto (GregorianCalendar.)
                  (.setTimeZone utc-tz))]
    (doto new-cal
      (.set Calendar/YEAR (.get cal Calendar/YEAR))
      (.set Calendar/MONTH (.get cal Calendar/MONTH))
      (.set Calendar/DAY_OF_MONTH (.get cal Calendar/DAY_OF_MONTH))
      (.set Calendar/HOUR_OF_DAY 0)
      (.set Calendar/MINUTE 0)
      (.set Calendar/SECOND 0)
      (.set Calendar/MILLISECOND 0))
    (.getTime new-cal)))

(defn days-ago-at-midnight
  "Returns a Java Date Object with a value of n days ago at midnight where
  n is the value passed to the function."
  [days]
  (-> days
    days-ago
    at-midnight))

(defn days-from-now-at-midnight
  "Returns a Java Date Object with a value of n days from now at midnight where
  n is the value passed to the function."
  [days]
  (-> days
    days-from-now
    at-midnight))

(defn strip-millis
  "Removes the millisecond precision from a Date object."
  [date]
  (-> (.getTime date ) (/ 1000) long (* 1000) long (java.util.Date.)))

(defn to-date-string
  "Formats date as 'yyyy-MM-dd'"
  [date]
  (format-datetime date-format date))

(defn from-date-string
  "Returns a Java Date Object from a 'yyyy-MM-dd' string"
  [date-string]
  (parse-datetime date-format date-string))

