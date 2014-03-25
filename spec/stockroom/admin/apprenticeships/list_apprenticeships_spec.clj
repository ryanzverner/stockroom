(ns stockroom.admin.apprenticeships.list-apprenticeships-spec
  (:require [chee.datetime :refer [days-ago]]
            [speclj.core :refer :all]
            [stockroom.admin.apprenticeships.index-view :as index-view]
            [stockroom.admin.apprenticeships.list-apprenticeships :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [month-day-year]]))

(def ten-days-ago (days-ago 10))
(def five-days-ago (days-ago 1))
(def four-days-ago (days-ago 4))
(def three-days-ago (days-ago 3))

(defn test-apprenticeships []
  [{:id 9
    :person-id 11
    :person {:first-name "John"
             :last-name "Doe"
             :email "johndoe@example.com"}
    :skill-level "resident"
    :start ten-days-ago
    :end four-days-ago
    :mentorships [{:person-id 12
                   :person {:first-name "Jane"
                            :last-name "Doe"
                            :email "johndoe@example.com"}
                   :start ten-days-ago
                   :end four-days-ago}]}
   {:id 40
    :person-id 60
    :person {:first-name "John"
             :last-name "Doe"
             :email "johndoe@example.com"}
    :skill-level "craftsman"
    :start five-days-ago
    :end three-days-ago
    :mentorships [{:person-id 13
                   :person {:first-name "Jane"
                            :last-name "Doe"
                            :email "johndoe@example.com"}
                   :start five-days-ago
                   :end three-days-ago}
                  {:person-id 13
                   :person {:first-name "Jane"
                            :last-name "Doe"
                            :email "johndoe@example.com"}
                   :start five-days-ago
                   :end three-days-ago}]}])

(describe "stockroom.admin.apprenticeships.list-apprenticeships"

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))

  (it "builds view data for the apprenticeships index view"
    (should= {:new-apprenticeship-url (urls/new-apprenticeship-url @ctx)
              :apprenticeships [{:id 9
                                 :person-id 11
                                 :person-name "John Doe"
                                 :mentors "Jane Doe"
                                 :skill-level "Resident"
                                 :start (month-day-year ten-days-ago)
                                 :end (month-day-year four-days-ago)}
                                {:id 40
                                 :person-id 60
                                 :person-name "John Doe"
                                 :mentors "Jane Doe, Jane Doe"
                                 :skill-level "Craftsman"
                                 :start (month-day-year five-days-ago)
                                 :end (month-day-year three-days-ago)}]}
             (build-view-data-for-apprenticeships-index-view {:apprenticeships (test-apprenticeships)
                                                              :context @ctx}))))
