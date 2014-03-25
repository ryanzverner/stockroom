(ns stockroom.admin.employment.form-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.employment.form :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight]]))

(def two-days-ago (days-ago-at-midnight 2))
(def three-days-ago (days-ago-at-midnight 3))

(def validate validate-employment-form)

(describe "stockroom.admin.employment.form"

  (with ctx (test-admin-context))

  (context "form validation"

    (it "invalid if start date is not present"
      (let [errors (validate {})]
        (should= [missing-start-date-error]
                 (:start errors))))

    (it "invalid if start date is not formatted correctly"
      (let [errors (validate {:start "something bad"})]
        (should= [invalid-date-error]
                 (:start errors))))

    (it "valid if start date is formatted correctly"
      (let [errors (validate {:start "1999-12-01"})]
        (should-be-nil (:start errors))))

    (it "valid if end date is not present"
      (let [errors (validate {})]
        (should-be-nil (:end errors))))

    (it "invalid if end date is not formatted correctly"
      (let [errors (validate {:end "something bad"})]
        (should= [invalid-date-error]
                 (:end errors))))

    (it "valid if end date is formatted correctly"
      (let [errors (validate {:end "1999-12-01"})]
        (should-be-nil (:end errors))))

    (it "invalid if position-id is not present"
      (let [errors (validate {:position-id ""})]
        (should= [missing-position-id-error]
                 (:position-id errors))))

    (it "valid if position-id is present"
      (let [errors (validate {:position-id "10"})]
        (should-be-nil (:position-id errors))))

    (it "invalid if person-id is not present"
      (let [errors (validate {:person-id ""})]
        (should= [missing-person-id-error]
                 (:person-id errors))))

    (it "valid if person-id is present"
      (let [errors (validate {:person-id "10"})]
        (should-be-nil (:person-id errors))))

    (it "invalid if location-id is not present"
      (let [errors (validate {:location-id ""})]
        (should= [missing-location-id-error]
                 (:location-id errors))))

    (it "valid if location-id is present"
      (let [errors (validate {:location-id "10"})]
        (should-be-nil (:location-id errors))))

    (it "returns no errors for valid data"
      (should= {}
               (validate {:position-id "10"
                          :person-id "10"
                          :start "1988-12-01"
                          :location-id "chicago"}))))

  (context "employment->form-params"
    (it "translates an existing employment into params for the form"
      (let [employment {:start two-days-ago
                        :end three-days-ago
                        :position-id 10
                        :person-id 11}]
        (should= {:start (date-for-input two-days-ago)
                  :end   (date-for-input three-days-ago)
                  :position-id "10"
                  :person-id   "11"}
                 (employment->form-params employment {}))))

    (it "overrides the employment values with request params"
      (let [employment {:start two-days-ago
                        :end three-days-ago
                        :position-id 10
                        :person-id 11}]
        (should= {:start "12/01/1993"
                  :end   "12/01/1998"
                  :position-id "50"
                  :person-id   "60"}
                 (employment->form-params employment {:start "12/01/1993"
                                                      :end "12/01/1998"
                                                      :position-id "50"
                                                      :person-id "60"})))))

  (it "builds view data for employment form"
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
      (should= {:errors errors
                :params params
                :position-options [["Select a Position" ""]
                                   ["Admin" "6"]
                                   ["Developer" "5"]]
                :person-options [["Select a Person" ""]
                                 ["John Smith" "10"]
                                 ["Sally Jones" "11"]]
                :location-options [["Select a Location" ""]
                                   ["Chicago" "50"]
                                   ["Denver" "51"]]
                :location-memberships [{:location "Chicago"
                                        :start "09/01/2016"}
                                       {:location "Denver"
                                        :start "05/01/2016"}]}
               (build-view-data-for-employment-form {:params params
                                                     :errors errors
                                                     :positions positions
                                                     :people people
                                                     :locations locations
                                                     :location-memberships location-memberships})))))
