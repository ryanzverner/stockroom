(ns stockroom.api.v1.people-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.response :refer [people-missing-first-name
                                                 people-missing-last-name]]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.api.v1.people :refer [create-person validate-person-params]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "/v1/people"

  (context "create-person"

    (describe "#validate-person-params"
      (it "returns empty list if params are valid"
        (let [params {:first-name "Michael"
                      :last-name "Scarn"}]
          (should= '() (validate-person-params params))))
      (it "returns a validation error if last name is missing"
        (should= people-missing-last-name (first (validate-person-params {:first-name "Meredith"}))))
      (it "returns a validation error if first name is missing"
        (should= people-missing-first-name (first (validate-person-params {:last-name "Palmer"})))))

    (it "creates a person"
      (let [request (-> (request :post "/v1/people")
                      (wring/set-user-api (test-stockroom-api))
                      (assoc :params {:first-name "Todd"
                                      :last-name "Packer"}))
            {person-id :body :as response} (handler request)
            api (wring/user-api response)
            {created-person :result} (api/find-person-by-id api person-id)]
        (should= 201      (:status response))
        (should= "Todd"   (:first-name created-person))
        (should= "Packer" (:last-name created-person))))

    (it "validates the person on create"
      (let [request (-> (request :post "/v1/people")
                      (wring/set-user-api (test-stockroom-api)))
            {status :status} (handler request)]
        (should= 422 status)))

    )

  (context "search-people"
    (it "searches for a person"
      (let [{api :api person1-id :result status :status} (api/create-person! (test-stockroom-api) {:first-name "Bob"
                                                                                                  :last-name "Smith"
                                                                                                  :email "fake1@null.com"})
            request (-> (request :get "/v1/people/search")
                      (assoc :params {:first-name "Bob"
                                      :last-name "Smith"
                                      :email "fake1@null.com"})
                      (wring/set-user-api api))
            response (handler request)
            people (:people (:body response))]
        (should= 200 (:status response))
        (should= 1 (count people))
        (should= person1-id (:id (first people)))))

    (it "does not use id, created_at or updated_at"
      (let [{api :api person-id :result status :status} (api/create-person! (test-stockroom-api) {:first-name "Bob"
                                                                                                   :last-name "Smith"
                                                                                                   :email "fake1@null.com"})
            {person :result} (api/find-person-by-id api person-id)
            request (-> (request :get "/v1/people/search")
                      (assoc :params {:id (:id person)
                                      :created-at (format-date-for-web (:created-at person))
                                      :updated-at (format-date-for-web (:updated-at person))})
                      (wring/set-user-api api))
            response (handler request)]
        (should= 422 (:status response))))

    )

  (context "/show-person"
    (it "finds a person by id"
      (let [{api :api person-id :result} (api/create-person! (test-stockroom-api) {:first-name "Brad"
                                                                                   :last-name "Doughbury"
                                                                                   :email "brad@doughbury.com"})
            {person :result} (api/find-person-by-id api person-id)
            request (-> (request :get (format "/v1/people/%s" person-id))
                        (wring/set-user-api api))
            response (handler request)]
        (should= 200 (:status response))
        (should= person (:body response)))))

  )

