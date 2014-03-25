(ns stockroom.admin.people.list-people-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.people.list-people :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.people.list-people"

  (with ctx (test-admin-context))

  (it "builds view data for index view"
    (let [people [{:id 10
                   :first-name "John"
                   :last-name "Smith"
                   :email "john@example.com"}
                  {:id 11
                   :first-name "Sally"
                   :last-name "Jones"
                   :email "sally@example.com"}]]
      (should= {:new-person-url (urls/new-person-url @ctx)
                :people [{:first-name "John"
                          :last-name "Smith"
                          :email "john@example.com"
                          :edit-url (urls/edit-person-url @ctx {:person-id 10})}
                         {:first-name "Sally"
                          :last-name "Jones"
                          :email "sally@example.com"
                          :edit-url (urls/edit-person-url @ctx {:person-id 11})}]}
               (build-view-data-for-index-view {:context @ctx
                                                :people people}))))

  )
