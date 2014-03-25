(ns stockroom.admin.people.edit-person-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.people.edit-person :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.people.edit-person"

  (with ctx (test-admin-context))

  (with person {:first-name "John"
                :last-name "Smith"
                :email "john@example.com"
                :id 10})

  (it "builds view data for the edit view"
    (let [params {:a 1}
          errors {:first-name ["one"]}]
      (should= {:update-person-url (urls/update-person-url @ctx {:person-id 10})
                :params {:first-name "John"
                         :last-name "Smith"
                         :email "john@example.com"}
                :errors errors}
               (build-view-data-for-edit-view {:context @ctx
                                               :params params
                                               :person @person
                                               :errors errors}))))

  (it "builds form params for a person"
    (let [params {:a 1}]
      (should= {:first-name "John"
                :last-name "Smith"
                :email "john@example.com"}
               (person->form-params @person params))))

  (it "overrides person attributes when params are given"
    (let [params {:first-name "Sally"
                  :last-name "Jones"
                  :email "sally@example.com"}]
      (should= {:first-name "Sally"
                :last-name "Jones"
                :email "sally@example.com"}
               (person->form-params @person params))))

  )
