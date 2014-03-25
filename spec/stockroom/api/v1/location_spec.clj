(ns stockroom.api.v1.location-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "/v1/locations"

  (with api (test-stockroom-api))

  (it "finds all locations"
    (let [{api :api location1-id :result} (api/create-location! @api {:name "location 1"})
          {location1 :result} (api/find-location-by-id api location1-id)
          {api :api location2-id :result} (api/create-location! api {:name "location 2"})
          {location2 :result} (api/find-location-by-id api location2-id)
          request (-> (request :get "/v1/locations")
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should== [location1 location2]
                (:locations (:body response)))))

  (it "finds a location by id"
    (let [{api :api location-id :result} (api/create-location! @api {:name "location 1"})
          {location :result} (api/find-location-by-id api location-id)
          request (-> (request :get (format "/v1/locations/%s" location-id))
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= location (:body response))))

  (it "creates a location"
    (let [request (-> (request :post "/v1/locations")
                    (wring/set-user-api @api)
                    (assoc :params {:name "test"}))
          {location-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-location :result} (api/find-location-by-id api location-id)]
      (should= 201 (:status response))
      (should= "test" (:name created-location)))))