(ns stockroom.admin.apprenticeships.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.apprenticeships.routes :refer [handler]]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def three-days-ago (days-ago-at-midnight 3))
(def two-days-ago (days-ago-at-midnight 2))

(describe "stockroom.admin.apprenticeships.routes"

  (with ctx (test-admin-context))
  (with api (test-stockroom-api))

  (it "renders the new apprenticeship page"
    (let [request (-> (request :get (urls/new-apprenticeship-url @ctx))
                      (wring/set-user-api @api))
          response ((handler @ctx) request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the apprenticeships index"
    (let [request (-> (request :get (urls/list-apprenticeships-url @ctx))
                      (wring/set-user-api @api))
          response ((handler @ctx) request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a new apprenticeship"
    (let [{api :api apprentice-id :result} (api/create-person! @api {:first-name "John"
                                                                     :last-name "Doe"
                                                                     :email "johndoe@example.com"})
          {api :api mentor-id :result} (api/create-person! api {:first-name "Jane"
                                                                :last-name "Doe"
                                                                :email "janedoe@example.com"})
          request (-> (request :post (urls/create-apprenticeship-url @ctx))
                      (wring/set-user-api api)
                      (assoc :params {:apprentice-id apprentice-id
                                      :apprenticeship-start-date (date-for-input three-days-ago)
                                      :apprenticeship-end-date (date-for-input two-days-ago)
                                      :skill-level "resident"
                                      :mentor-id mentor-id
                                      :mentorship-start-date (date-for-input three-days-ago)
                                      :mentorship-end-date (date-for-input two-days-ago)}))
          response ((handler @ctx) request)
          api (wring/user-api response)
          created-apprenticeship (first (:result (api/find-all-apprenticeships api)))
          created-mentorship (first (:mentorships created-apprenticeship))]
      (should= 302 (:status response))
      (should= apprentice-id (:person-id created-apprenticeship))
      (should= three-days-ago (:start created-apprenticeship))
      (should= two-days-ago (:end created-apprenticeship))
      (should= "resident" (:skill-level created-apprenticeship))
      (should= mentor-id (:person-id created-mentorship))
      (should= three-days-ago (:start created-mentorship))
      (should= two-days-ago (:end created-mentorship)))))
