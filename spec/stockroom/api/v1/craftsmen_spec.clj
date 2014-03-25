(ns stockroom.api.v1.craftsmen-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def one-day-ago (days-ago-at-midnight 1))
(def one-day-from-now (days-from-now-at-midnight 1))

(describe "/v1/craftsmen"

  (with api (test-stockroom-api))

  (describe "Getting the current craftsmen"

    (defn get-current-craftsmen [api]
      (let [request (-> (request :get "/v1/craftsmen/current")
                        (wring/set-user-api api))]
        (handler request)))

    (it "returns an empty list when there are no craftsmen"
      (let [response (get-current-craftsmen @api)]
        (should= 200 (:status response))
        (should= [] (:body response))))

    (it "returns the only craftsman"
      (let [{api :api person-id :result} (api/create-person! @api {:first-name "Eric"})
            {api :api position-id :result} (api/create-employment-position! api {:name "craftsman"})
            {api :api location-id :result} (api/create-location! api {:name "chicago"})
            {api :api employment-id :result} (api/create-employment! api {:person-id person-id
                                                                          :position-id position-id
                                                                          :location-id location-id
                                                                          :start one-day-ago
                                                                          :end one-day-from-now})
            response (get-current-craftsmen api)]
        (should= 1 (count (:body response)))
        (should= "Eric" (:first-name (first (:body response))))))

    (it "does not return a non-craftsman"
      (let [{api :api person-id :result} (api/create-person! @api {:first-name "Eric"})
            {api :api position-id :result} (api/create-employment-position! api {:name "resident"})
            {api :api location-id :result} (api/create-location! api {:name "chicago"})
            {api :api employment-id :result} (api/create-employment! api {:person-id person-id
                                                                 :position-id position-id
                                                                 :location-id location-id
                                                                 :start one-day-ago
                                                                 :end one-day-from-now})
            response (get-current-craftsmen api)]
        (should= [] (:body response))))

    (it "includes the current location"
      (let [{api :api person-id :result} (api/create-person! @api {:first-name "Eric"})
            {api :api position-id :result} (api/create-employment-position! api {:name "craftsman"})
            {api :api location-id :result} (api/create-location! api {:name "Chicago"})
            {api :api employment-id :result} (api/create-employment! api {:person-id person-id
                                                                          :position-id position-id
                                                                          :location-id location-id
                                                                          :start one-day-ago
                                                                          :end one-day-from-now})
            {api :api membership-id :result} (api/create-location-membership! api employment-id location-id {:start one-day-ago})
            response (get-current-craftsmen api)]
        (should= "Chicago" (:name (:current-location (first (:body response)))))))))
