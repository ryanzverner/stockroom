(ns stockroom.admin.employment.routes-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.employment.routes :refer [handler]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def two-days-ago (days-ago-at-midnight 2))
(def three-days-ago (days-ago-at-midnight 3))
(def ten-days-ago (days-ago-at-midnight 10))

(defn test-employment [api]
  (let [{api :api person-id :result} (api/create-person! api {:first-name "John"})
        {api :api position-id :result} (api/create-employment-position! api {:name "Dev"})
        {api :api location-id :result} (api/create-location! api {:name "Chicago"})
        response (api/create-employment! api {:person-id person-id
                                              :position-id position-id
                                              :start two-days-ago
                                              :end three-days-ago
                                              :location-id location-id})]
    [(:api response) (:result response)]))

(describe "stockroom.admin.employment.routes"

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with employment (handler @ctx))

  (it "renders the employment index"
    (let [request (-> (request :get (urls/list-employments-url @ctx))
                    (wring/set-user-api @api))
          response (@employment request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the new employment page"
    (let [request (-> (request :get (urls/new-employment-url @ctx))
                    (wring/set-user-api @api))
          response (@employment request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a new employment"
    (let [{api :api person-id :result} (api/create-person! @api {:first-name "John"})
          {api :api position-id :result} (api/create-employment-position! api {:name "Dev"})
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          start two-days-ago
          end   three-days-ago
          request (-> (request :post (urls/create-employment-url @ctx))
                    (wring/set-user-api api)
                    (assoc :params {:person-id person-id
                                    :position-id position-id
                                    :start (date-for-input start)
                                    :end   (date-for-input end)
                                    :location-id location-id}))
          response (@employment request)
          api (wring/user-api response)
          created-employment (first (:result (api/find-all-employments api {})))]
      (should= start (:start created-employment))
      (should= end (:end created-employment))
      (should= position-id (:position-id created-employment))
      (should= person-id (:person-id created-employment))
      (should-redirect-to response (urls/list-employments-url @ctx))
      (should= "Successfully created employment."
               (-> response :flash :success))))

  (it "re-renders when validation errors"
    (let [request (-> (request :post (urls/create-employment-url @ctx))
                    (wring/set-user-api @api))
          response (@employment request)
          api (wring/user-api response)
          created-employment (first (:result (api/find-all-employments api {})))]
      (should-be-nil created-employment)
      (should= 422 (:status response))))

  (it "re-renders when the api call fails"
    (let [start two-days-ago
          request (-> (request :post (urls/create-employment-url @ctx))
                    (wring/set-user-api @api)
                    (assoc :params {:person-id "abc"
                                    :position-id "def"
                                    :start (date-for-input start)
                                    :end ""}))
          response (@employment request)
          api (wring/user-api response)
          created-employment (first (:result (api/find-all-employments api {})))]
      (should-be-nil created-employment)
      (should= 422 (:status response))))

  (it "renders the edit employment page"
    (let [[api employment-id] (test-employment @api)
          request (-> (request :get (urls/edit-employment-url @ctx {:employment-id employment-id}))
                    (wring/set-user-api api))
          response (@employment request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders not found when the employment cannot be found"
    (let [request (-> (request :get (urls/edit-employment-url @ctx {:employment-id "unknown"}))
                    (wring/set-user-api @api))
          response (@employment request)]
      (should-render-not-found response)))

  (it "updates an employment"
    (let [[api employment-id] (test-employment @api)
          {created-employment :result} (api/find-employment-by-id api employment-id)
          new-start ten-days-ago
          request (-> (request :put (urls/update-employment-url @ctx {:employment-id employment-id}))
                    (wring/set-user-api api)
                    (assoc :params {:start (date-for-input new-start)
                                    :end   (date-for-input (:end created-employment))
                                    :position-id (:position-id created-employment)
                                    :person-id (:person-id created-employment)
                                    :location-id (:location-id created-employment)}))
          response (@employment request)
          api (wring/user-api response)
          {updated-employment :result} (api/find-employment-by-id api employment-id)]
      (should= new-start (:start updated-employment))
      (should= (:end created-employment) (:end updated-employment))
      (should= (:position-id created-employment) (:position-id updated-employment))
      (should= (:person-id created-employment) (:person-id updated-employment))
      (should-redirect-to response (urls/list-employments-url @ctx))
      (should= "Successfully updated employment."
               (-> response :flash :success))))

  (it "deletes a location membership from an employment"
    (let [[api employment-id] (test-employment @api)
          {api :api location-id :result} (api/create-location! api {:name "test location"})
          {api :api location-membership-id :result} (api/create-location-membership! api employment-id location-id {:start two-days-ago})
          request (-> (request :delete (urls/delete-location-membership-url @ctx {:employment-id employment-id})
                               {:params {:location-membership-id location-membership-id}})
                    (wring/set-user-api api))
          response (@employment request)]
      (should-redirect-after-post-to response (urls/edit-employment-url @ctx {:employment-id employment-id}))
      (should= "Removed location association." (:success (:flash response)))))

  (it "adds a location membership to an employment"
    (let [[api employment-id] (test-employment @api)
          {api :api location-id :result} (api/create-location! api {:name "test location"})
          start two-days-ago
          request (-> (request :post (urls/create-location-membership-url @ctx {:employment-id employment-id}))
                    (wring/set-user-api api)
                    (assoc :params {:employment-id employment-id
                                    :location-id location-id
                                    :start (date-for-input start)}))
          response (@employment request)
          api (wring/user-api response)
          created-location-membership (last (:result (api/find-all-location-memberships-for-employment api employment-id)))]
      (should= start (:start created-location-membership))
      (should= location-id (:location-id created-location-membership))
      (should= employment-id (:employment-id created-location-membership))
      (should-redirect-to response (urls/edit-employment-url @ctx {:employment-id employment-id}))
      (should= "Successfully created Location Assignment."
               (-> response :flash :success))))

  (it "re-renders when validation errors"
    (let [[api employment-id] (test-employment @api)
          request (-> (request :put (urls/update-employment-url @ctx {:employment-id employment-id}))
                    (wring/set-user-api api))
          response (@employment request)
          api (wring/user-api response)]
      (should= 422 (:status response))))

  (it "re-renders when the api call fails"
    (let [[api employment-id] (test-employment @api)
          start two-days-ago
          request (-> (request :put (urls/update-employment-url @ctx {:employment-id employment-id}))
                    (wring/set-user-api api)
                    (assoc :params {:person-id "abc"
                                    :position-id "def"
                                    :start (date-for-input start)
                                    :end ""}))
          response (@employment request)
          api (wring/user-api response)]
      (should= 422 (:status response)))))