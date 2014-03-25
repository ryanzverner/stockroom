(ns stockroom.api.v1.clients-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "/v1/clients"

  (with api (test-stockroom-api))

  (it "finds all clients"
    (let [{api :api client1-id :result} (api/create-client! @api {:name "client 1"})
          {client1 :result} (api/find-client-by-id api client1-id)
          {api :api client2-id :result} (api/create-client! api {:name "client 2"})
          {client2 :result} (api/find-client-by-id api client2-id)
          request (-> (request :get "/v1/clients")
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should== [client1 client2]
                (:clients (:body response)))))

  (it "finds a client by id"
    (let [{api :api client-id :result} (api/create-client! @api {:name "client 1"})
          {client :result} (api/find-client-by-id api client-id)
          request (-> (request :get (format "/v1/clients/%s" client-id))
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= client (:body response))))

  (it "creates a client"
    (let [request (-> (request :post "/v1/clients")
                    (wring/set-user-api @api)
                    (assoc :params {:name "test"}))
          {client-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-client :result} (api/find-client-by-id api client-id)]
      (should= 201 (:status response))
      (should= "test" (:name created-client))))

  (it "updates a client"
    (let [{api :api client-id :result} (api/create-client! @api {})
          request (-> (request :put (format "/v1/clients/%s" client-id))
                    (wring/set-user-api api)
                    (assoc :params {:name "test"}))
          response (handler request)
          api (wring/user-api response)
          {updated-client :result} (api/find-client-by-id api client-id)]
      (should= 200 (:status response))
      (should= "test" (:name updated-client))))

  )
