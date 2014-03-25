(ns stockroom.admin.form-helper-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.form-helper :refer :all]))

(describe "stockroom.admin.form-helper"

  (it "displays an empty list when there are no people records"
    (let [people []]
      (should= (list ["Select a Person" ""])
               (person-select-options people))))

  (it "displays a list of people sorted alphabetically"
    (let [people [{:id 1
                   :first-name "Minnie"
                   :last-name "Mouse"}
                   {:id 2
                    :first-name "Donald"
                    :last-name "Duck"}]]
      (should= (list ["Select a Person" ""] ["Donald Duck" "2"] ["Minnie Mouse" "1"])
               (person-select-options people))))

  (it "displays an empty list when there are no position records"
    (let [positions []]
      (should= (list ["Select a Position" ""])
               (position-select-options positions))))

  (it "displays a list of positions sorted alphabetically"
    (let [positions [{:id 1
                      :name "Resident"}
                     {:id 2
                      :name "Crafter"}]]
      (should= (list ["Select a Position" ""] ["Crafter" "2"] ["Resident" "1"])
               (position-select-options positions))))

  (it "displays an empty list when there are no location records"
    (let [locations []]
      (should= (list ["Select a Location" ""])
               (location-select-options locations))))

  (it "displays a list of locations sorted alphabetically"
    (let [locations [{:id 1
                      :name "Chicago"}
                     {:id 2
                      :name "London"}]]
      (should= (list ["Select a Location" ""] ["Chicago" "1"] ["London" "2"])
               (location-select-options locations)))))