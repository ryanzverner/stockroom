(ns stockroom.admin.sows.form-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.sows.form :refer :all]))

(def validate validate-sow-form)

(describe "stockroom.admin.sows.form"

  (context "form validation"

    (it "invalid if start date is not present"
      (let [errors (validate {})]
        (should= [missing-start-date-error]
                 (:start errors))))

    (it "invalid if start date is not formatted correctly"
      (let [errors (validate {:start "something bad"})]
        (should= [invalid-date-error]
                 (:start errors))))

    (it "invalid if hourly rate is not present"
      (let [errors (validate {})]
        (should= [missing-hourly-rate-error] (:hourly-rate errors))))

    (it "invalid if no checkbox is selected"
      (let [errors (validate {})]
        (should= [no-project-selected-error] (:projects errors))))))
