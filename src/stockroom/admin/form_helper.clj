(ns stockroom.admin.form-helper
  (:require [stockroom.admin.util.view-helper :refer :all]))

(defn person-select-options [people]
  (cons ["Select a Person" ""]
        (sort-by first
                 (map
                   (fn [{:keys [first-name last-name id]}]
                     [(first-last-name first-name last-name) (str id)])
                   people))))

(defn position-select-options [positions]
  (cons ["Select a Position" ""]
       (sort-by first
                (map
                  (fn [position]
                    [(:name position) (str (:id position))])
                  positions))))

(defn location-select-options [locations]
  (cons ["Select a Location" ""]
       (sort-by first
                (map
                  (fn [location]
                    [(:name location) (str (:id location))])
                  locations))))