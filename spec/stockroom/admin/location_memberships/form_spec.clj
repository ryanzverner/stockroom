(ns stockroom.admin.location-memberships.form-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.location-memberships.form :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight]]))

(def two-days-ago (days-ago-at-midnight 2))
(def three-days-ago (days-ago-at-midnight 3))
(def bout-a-week-ago (days-ago-at-midnight 7))

(def validate validate-location-membership-form)

(describe "stockroom.admin.location-memberships.form"

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

    (it "invalid if location-id is not present"
      (let [errors (validate {:location-id ""})]
        (should= [missing-location-id-error]
                 (:location-id errors))))

    (it "valid if location-id is present"
      (let [errors (validate {:location-id "10"})]
        (should-be-nil (:location-id errors))))

    (it "invalid if employment-id is not present"
      (let [errors (validate {:employment-id ""})]
        (should= [missing-employment-id-error]
                 (:employment-id errors))))

    (it "valid if employment-id is present"
      (let [errors (validate {:employment-id "10"})]
        (should-be-nil (:employment-id errors))))

    (it "returns no errors for valid data"
      (should= {}
               (validate {:location-id "10"
                          :employment-id "10"
                          :start "1988-12-01"}))))

  (context "location-membership->form-params"
    (it "translates an existing location membership into params for the form"
      (let [location-membership {:start two-days-ago
                                 :location-id 10
                                 :employment-id 11}]
        (should= {:start (date-for-input two-days-ago)
                  :location-id "10"
                  :employment-id "11"}
                 (location-membership->form-params location-membership {}))))

    (it "overrides the location membership values with request params"
      (let [location-membership {:start two-days-ago
                        :end three-days-ago
                        :location-id 10
                        :employment-id 11}]
        (should= {:start "12/01/1993"
                  :location-id "50"
                  :employment-id "60"}
                 (location-membership->form-params location-membership {:start "12/01/1993"
                                                                        :location-id "50"
                                                                        :employment-id "60"})))))

  (it "builds view data for location membership form"
    (let [people [{:id 11 :first-name "Sally" :last-name "Jones"}
                  {:id 10 :first-name "John" :last-name "Smith"}]
          positions [{:id 20
                      :name "Resident"}
                     {:id 21
                      :name "Crafter"}]
          employment {:id 11
                      :position-id 20
                      :person-id (get (first people) :id)
                      :start "09/01/2015"
                      :end "02/01/2016"}
          locations [{:id 5
                      :name "Chicago"}
                     {:id 6
                      :name "London"}]
          location-memberships [{:location-id 5
                                 :employment-id 11
                                 :start bout-a-week-ago}
                                 {:location-id 6
                                  :employment-id 11
                                  :start bout-a-week-ago}]
          params {:a 1 :b 2}
          errors {:a ["one" "two"]}]
      (should= {:errors errors
                :params params
                :location-options [["Select a Location" ""]
                                   ["Chicago" "5"]
                                   ["London" "6"]]
                :employment-id 11}
               (build-view-data-for-location-membership-form {:params params
                                                              :errors errors
                                                              :locations locations
                                                              :employment-id (get employment :id)
                                                              :location-memberships location-memberships
                                                              :request ""})))))
