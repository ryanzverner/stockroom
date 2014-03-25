(ns stockroom.admin.people.form-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.people.form :refer :all]))

(def validate validate-person-form)

(describe "stockroom.admin.people.form"

  (context "form validation"

    (it "invalid if first-name is not present"
      (let [errors (validate {})]
        (should= [missing-first-name-error] (:first-name errors))))

    (it "invalid if last-name is not present"
      (let [errors (validate {})]
        (should= [missing-last-name-error] (:last-name errors))))

    (it "invalid if email is not present"
      (let [errors (validate {})]
        (should= [missing-email-error] (:email errors))))

    (it "invalid if email is not an email"
      (let [errors (validate {:email "not an email"})]
        (should= [invalid-email-error] (:email errors))))

    (it "valid when everything is correct"
      (let [errors (validate {:first-name "John"
                              :last-name "Smith"
                              :email "john@example.com"})]
        (should= {} errors)))

    )

  )
