(ns stockroom.admin.employment.new-employment-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.employment.new-employment :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.employment.new-employment"

  (with ctx (test-admin-context))

  (it "builds view data for new employment view"
    (let [people [{:id 11 :first-name "Sally" :last-name "Jones"}
                  {:id 10 :first-name "John" :last-name "Smith"}]
          positions [{:id 5 :name "Developer"}
                     {:id 6 :name "Admin"}]
          locations [{:id 50 :name "Chicago"}
                     {:id 51 :name "Denver"}]
          params {:a 1 :b 2}
          errors {:a ["one" "two"]}
          location-memberships [{:location "Chicago"
                                 :start "09/01/2016"}
                                {:location "Denver"
                                 :start "05/01/2016"}]]
      (should= {:create-employment-url (urls/create-employment-url @ctx)
                :errors errors
                :params params
                :position-options [["Select a Position" ""]
                                   ["Admin" "6"]
                                   ["Developer" "5"]]
                :person-options [["Select a Person" ""]
                                 ["John Smith" "10"]
                                 ["Sally Jones" "11"]]
                :location-memberships [{:location "Chicago"
                                        :start "09/01/2016"}
                                       {:location "Denver"
                                        :start "05/01/2016"}]
                :location-options [["Select a Location" ""]
                                   ["Chicago" "50"]
                                   ["Denver" "51"]]}
               (build-view-data-for-new-employment-view {:context @ctx
                                                         :params params
                                                         :errors errors
                                                         :positions positions
                                                         :people people
                                                         :locations locations
                                                         :location-memberships location-memberships})))))