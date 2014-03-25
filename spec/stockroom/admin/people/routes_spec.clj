(ns stockroom.admin.people.routes-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.people.routes :refer [handler]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "stockroom.admin.people.routes"

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with people (handler @ctx))

  (it "renders the index page"
    (let [request (-> (request :get (urls/list-people-url @ctx))
                    (wring/set-user-api @api))
          response (@people request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the new page"
    (let [request (-> (request :get (urls/new-person-url @ctx))
                    (wring/set-user-api @api))
          response (@people request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the edit page"
    (let [{api :api person-id :result} (api/create-person! @api {})
          request (-> (request :get (urls/edit-person-url @ctx {:person-id person-id}))
                    (wring/set-user-api api))
          response (@people request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "edit responds with not found if the person does not exist"
    (let [request (-> (request :get (urls/edit-person-url @ctx {:person-id "abc"}))
                    (wring/set-user-api @api))
          response (@people request)]
      (should-render-not-found response)))

  (it "creates a person"
    (let [request (-> (request :post (urls/create-person-url @ctx))
                    (wring/set-user-api @api)
                    (assoc :params {:first-name "John"
                                    :last-name "Smith"
                                    :email "john@example.com"}))
          response (@people request)
          api (wring/user-api response)
          created-person (first (:result (api/find-all-people api)))]
      (should= "John" (:first-name created-person))
      (should= "Smith" (:last-name created-person))
      (should= "john@example.com" (:email created-person))
      (should-redirect-to response (urls/list-people-url @ctx))
      (should= "Successfuly created person."
               (-> response :flash :success))))

  (it "re-renders when validation errors"
    (let [request (-> (request :post (urls/create-person-url @ctx))
                    (wring/set-user-api @api))
          response (@people request)
          api (wring/user-api response)]
      (should-not (seq (:result (api/find-all-people api))))
      (should= 422 (:status response))))

  (it "updates a person"
    (let [{api :api person-id :result} (api/create-person! @api {:first-name "John"})
          {created-person :result} (api/find-person-by-id api person-id)
          request (-> (request :put (urls/update-person-url @ctx {:person-id person-id}))
                    (wring/set-user-api api)
                    (assoc :params {:first-name "New John"
                                    :last-name "New Smith"
                                    :email "new@example.com"}))
          response (@people request)
          api (wring/user-api response)
          {updated-person :result} (api/find-person-by-id api person-id)]
      (should-not= created-person updated-person)
      (should-redirect-to response (urls/list-people-url @ctx))
      (should= "Successfuly updated person."
               (-> response :flash :success))))

  (it "re-renders when validation errors"
    (let [{api :api person-id :result} (api/create-person! @api {:first-name "John"})
          request (-> (request :put (urls/update-person-url @ctx {:person-id person-id}))
                    (wring/set-user-api api)
                    (assoc :params {:first-name "New John"}))
          response (@people request)
          api (wring/user-api response)]
      (should= 422 (:status response))))

  )
