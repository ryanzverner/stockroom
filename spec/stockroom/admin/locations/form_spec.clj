(ns stockroom.admin.locations.form-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.locations.form :refer :all]))

(def validate validate-location-form)

(describe "stockroom.admin.locations.form"

  (context "form validation"

    (it "invalid if name is not present"
      (let [errors (validate {})]
        (should= [missing-name-error] (:name errors))))

    (it "valid when everything is correct"
      (let [errors (validate {:name "Chicago"})]
        (should= {} errors)))))