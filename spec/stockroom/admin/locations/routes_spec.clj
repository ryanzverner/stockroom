(ns stockroom.admin.locations.routes-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.locations.routes :refer [handler]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "stockroom.admin.locations.routes"

  (with ctx (test-admin-context))
  (with api (test-stockroom-api))
  (with locations (handler @ctx))

  (it "renders the new location page"
    (let [request (-> (request :get (urls/new-location-url @ctx))
                      (wring/set-user-api @api))
          response ((handler @ctx) request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the locations index"
    (let [request (-> (request :get (urls/list-locations-url @ctx))
                      (wring/set-user-api @api))
          response ((handler @ctx) request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))


  (it "creates a location"
    (let [request (-> (request :post (urls/create-location-url @ctx) {:params {:name "Seattle"}})
                    (wring/set-user-api @api))
          response (@locations request)
          api (wring/user-api response)
          {locations :result} (api/find-all-locations api)
          created-location (first locations)]
      (should= 1 (count locations))
      (should= "Seattle" (:name created-location))
      (should-redirect-to response (urls/list-locations-url @ctx))
      (should= "Successfully created location." (:success (:flash response))))))