(ns stockroom.api.v1.employments-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.response :refer [employments-malformatted-end-date
                                                 employments-malformatted-start-date]]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.util.time :refer [days-ago-at-midnight
                                         days-from-now-at-midnight
                                         to-date-string]]
            [stockroom.v1.api :as api]
            [stockroom.v1.response :refer [employment-invalid-person-id
                                           employment-invalid-position-id
                                           employment-missing-start-date]]
            [stockroom.v1.ring :as wring]))

(def three-days-ago (days-ago-at-midnight 3))
(def four-days-ago (days-ago-at-midnight 4))
(def five-days-ago (days-ago-at-midnight 5))
(def ten-days-ago (days-ago-at-midnight 10))

(def three-days-from-now (days-from-now-at-midnight 3))
(def five-days-from-now (days-from-now-at-midnight 5))
(def ten-days-from-now (days-from-now-at-midnight 10))

(describe "/v1/employments"

  (with api (test-stockroom-api))

  (it "finds all employments"
    (let [{api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (api/create-employment-position! api {:name "Admin"})
          {api :api admin :result} (api/find-employment-position-by-id api admin-id)
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          {api :api location :result} (api/find-location-by-id api location-id)
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api sally-id :result} (api/create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (api/find-person-by-id api sally-id)
          {api :api employment1-id :result} (api/create-employment! api {:person-id john-id
                                                                         :position-id developer-id
                                                                         :location-id location-id
                                                                         :start ten-days-ago
                                                                         :end four-days-ago})
          {api :api employment1 :result} (api/find-employment-by-id api employment1-id)
          {api :api employment2-id :result} (api/create-employment! api {:person-id sally-id
                                                                         :position-id admin-id
                                                                         :location-id location-id
                                                                         :start five-days-ago
                                                                         :end three-days-ago})
          {api :api employment2 :result} (api/find-employment-by-id api employment2-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc employment1 :person john :position developer)
                 (assoc employment2 :person sally :position admin)]
                (:employments (:body response)))))

  (it "finds all employments for a location"
    (let [{api :api resident-id :result} (api/create-employment-position! @api {:name "Resident"})
          {api :api resident :result} (api/find-employment-position-by-id api resident-id)
          {api :api developer-id :result} (api/create-employment-position! api {:name "Developer"})
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (api/create-employment-position! api {:name "Admin"})
          {api :api admin :result} (api/find-employment-position-by-id api admin-id)
          {api :api location1-id :result} (api/create-location! api {:name "Chicago"})
          {api :api location1 :result} (api/find-location-by-id api location1-id)
          {api :api location2-id :result} (api/create-location! api {:name "London"})
          {api :api location2 :result} (api/find-location-by-id api location2-id)
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api sally-id :result} (api/create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (api/find-person-by-id api sally-id)
          {api :api employment1-id :result} (api/create-employment! api {:person-id john-id
                                                                         :position-id resident-id
                                                                         :location-id location1-id
                                                                         :start ten-days-ago
                                                                         :end four-days-ago})
          {api :api employment1 :result} (api/find-employment-by-id api employment1-id)
          {api :api employment2-id :result} (api/create-employment! api {:person-id sally-id
                                                                         :position-id admin-id
                                                                         :location-id location2-id
                                                                         :start five-days-ago
                                                                         :end three-days-ago})
          {api :api employment2 :result} (api/find-employment-by-id api employment2-id)
          {api :api employment3-id :result} (api/create-employment! api {:person-id john-id
                                                                         :position-id developer-id
                                                                         :location-id location1-id
                                                                         :start four-days-ago
                                                                         :end three-days-ago})
          {api :api employment3 :result} (api/find-employment-by-id api employment3-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:location-id location1-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc employment1 :person john :position resident)
                 (assoc employment3 :person john :position developer)]
                (:employments (:body response)))))

  (it "filters out employments that ended before the start date"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api resident-id :result} (api/create-employment-position! @api {:name "Resident"})
          {api :api resident :result} (api/find-employment-position-by-id api resident-id)
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api ends-before-start-date-id :result} (api/create-employment! api {:person-id john-id
                                                                                    :position-id resident-id
                                                                                    :start ten-days-ago
                                                                                    :end four-days-ago})
          {api :api ends-before-start-date :result} (api/find-employment-by-id api ends-before-start-date-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [] (:employments (:body response)))))


  (it "filters out employments that start after the end date"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api admin-id :result} (api/create-employment-position! @api {:name "Admin"})
          {api :api admin :result} (api/find-employment-position-by-id api admin-id)
          {api :api sally-id :result} (api/create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (api/find-person-by-id api sally-id)
          {api :api starts-after-end-date-id :result} (api/create-employment! api {:person-id sally-id
                                                                                :position-id admin-id
                                                                                :start five-days-from-now
                                                                                :end ten-days-from-now})
          {api :api starts-after-end-date :result} (api/find-employment-by-id api starts-after-end-date-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [] (:employments (:body response)))))

  (it "finds all employments for a date range"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api resident-id :result} (api/create-employment-position! @api {:name "Resident"})
          {api :api resident :result} (api/find-employment-position-by-id api resident-id)
          {api :api developer-id :result} (api/create-employment-position! api {:name "Developer"})
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (api/create-employment-position! api {:name "Admin"})
          {api :api admin :result} (api/find-employment-position-by-id api admin-id)
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api sally-id :result} (api/create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (api/find-person-by-id api sally-id)
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api ends-before-start-date-id :result}
            (api/create-employment! api {:person-id john-id
                                         :position-id resident-id
                                         :start ten-days-ago
                                         :end four-days-ago
                                         :location-id chicago-id})
          {api :api ends-before-start-date :result} (api/find-employment-by-id api ends-before-start-date-id)
          {api :api starts-after-end-date-id :result}
            (api/create-employment! api {:person-id sally-id
                                         :position-id admin-id
                                         :start five-days-from-now
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api starts-after-end-date :result} (api/find-employment-by-id api starts-after-end-date-id)
          {api :api within-date-range-id :result}
            (api/create-employment! api {:person-id john-id
                                         :position-id developer-id
                                         :start ten-days-ago
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api within-date-range :result} (api/find-employment-by-id api within-date-range-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc within-date-range :person john :position developer)]
                (:employments (:body response)))))

(it "does not return a current employment for a date range and location-id if the location-membership ends before the start date"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api london-id :result} (api/create-location! api {:name "London"})
          {api :api within-date-range-id :result}
            (api/create-employment! api {:person-id john-id
                                         :position-id developer-id
                                         :start ten-days-ago
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api within-date-range :result} (api/find-employment-by-id api within-date-range-id)
          {api :api london-location-membership-id :result} (api/create-location-membership! api within-date-range-id london-id {:start five-days-ago})
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date :location-id chicago-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [] (:employments (:body response)))))

(it "does not return a current employment for a date range and location-id if the location-membership begins after the end date"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api within-date-range-id :result} (api/create-employment! api {:person-id john-id
                                                                               :position-id developer-id
                                                                               :start ten-days-ago
                                                                               :end ten-days-from-now})
          {api :api within-date-range :result} (api/find-employment-by-id api within-date-range-id)
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api chicago :result} (api/find-location-by-id api chicago-id)
          {api :api london-id :result} (api/create-location! api {:name "London"})
          {api :api london :result} (api/find-location-by-id api london-id)
          {api :api chicago-location-membership-id :result} (api/create-location-membership! api within-date-range-id chicago-id {:start ten-days-ago})
          {api :api london-location-membership-id :result} (api/create-location-membership! api within-date-range-id london-id {:start five-days-from-now})
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date :location-id london-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [] (:employments (:body response)))))

(it "finds all employments for a date range and a location-id"
    (let [start-date (to-date-string three-days-ago)
          end-date (to-date-string three-days-from-now)
          {api :api resident-id :result} (api/create-employment-position! @api {:name "Resident"})
          {api :api resident :result} (api/find-employment-position-by-id api resident-id)
          {api :api developer-id :result} (api/create-employment-position! api {:name "Developer"})
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (api/create-employment-position! api {:name "Admin"})
          {api :api admin :result} (api/find-employment-position-by-id api admin-id)
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api sally-id :result} (api/create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (api/find-person-by-id api sally-id)
          {api :api jane-id :result} (api/create-person! api {:first-name "Jane" :last-name "Doe"})
          {api :api jane :result} (api/find-person-by-id api jane-id)
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api london-id :result} (api/create-location! api {:name "London"})
          {api :api ends-before-start-date-id :result}
            (api/create-employment! api {:person-id john-id
                                         :position-id resident-id
                                         :start ten-days-ago
                                         :end four-days-ago
                                         :location-id chicago-id})
          {api :api ends-before-start-date :result}
            (api/find-employment-by-id api ends-before-start-date-id)
          {api :api starts-after-end-date-id :result}
            (api/create-employment! api {:person-id sally-id
                                         :position-id admin-id
                                         :start five-days-from-now
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api starts-after-end-date :result}
            (api/find-employment-by-id api starts-after-end-date-id)
          {api :api within-date-range-moved-to-london-id :result}
            (api/create-employment! api {:person-id john-id
                                         :position-id developer-id
                                         :start ten-days-ago
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api within-date-range-moved-to-london :result}
            (api/find-employment-by-id api within-date-range-moved-to-london-id)
          {api :api within-date-range-currently-in-chicago-id :result}
            (api/create-employment! api {:person-id jane-id
                                         :position-id developer-id
                                         :start ten-days-ago
                                         :end ten-days-from-now
                                         :location-id chicago-id})
          {api :api within-date-range-currently-in-chicago :result}
            (api/find-employment-by-id api within-date-range-currently-in-chicago-id)
          {api :api started-in-chicago-id :result}
            (api/create-location-membership! api within-date-range-moved-to-london-id london-id {:start five-days-ago})
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date :location-id chicago-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc within-date-range-currently-in-chicago :person jane :position developer)]
                (:employments (:body response)))))

  (it "returns an employment for a date range and location-id when the employment end is null"
      (let [start-date three-days-ago
            end-date three-days-from-now
          {api :api developer-id :result} (api/create-employment-position! @api {:name "Devs;dkfjas;dlfjeloper"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api within-date-range-id :result} (api/create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :location-id chicago-id})
          {api :api within-date-range :result} (api/find-employment-by-id api within-date-range-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date :location-id chicago-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc within-date-range :person john :position developer)]
                (:employments (:body response)))))

(it "returns an employment for a date range and location-id when the location id is a string"
      (let [start-date three-days-ago
            end-date three-days-from-now
          {api :api developer-id :result} (api/create-employment-position! @api {:name "Devs;dkfjas;dlfjeloper"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (api/find-person-by-id api john-id)
          {api :api developer :result} (api/find-employment-position-by-id api developer-id)
          {api :api chicago-id :result} (api/create-location! api {:name "Chicago"})
          {api :api within-date-range-id :result} (api/create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :location-id (str chicago-id)})
          {api :api within-date-range :result} (api/find-employment-by-id api within-date-range-id)
          request (-> (request :get "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:start-date start-date :end-date end-date :location-id chicago-id}))
          response (handler request)]
      (should= 200 (:status response))
      (should== [(assoc within-date-range :person john :position developer)]
                (:employments (:body response)))))

  (it "creates an employment"
    (let [{api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api stanley-id :result} (api/create-person! api {:first-name "Stanley" :last-name "Hudson"})
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          request (-> (request :post "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:person-id stanley-id
                                    :position-id developer-id
                                    :location-id location-id
                                    :start (format-date-for-web five-days-ago)
                                    :end (format-date-for-web three-days-ago)}))
          {employment-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-employment :result} (api/find-employment-by-id api employment-id)]
      (should= 201 (:status response))
      (should= stanley-id (:person-id created-employment))
      (should= developer-id (:position-id created-employment))
      (should= five-days-ago (:start created-employment))
      (should= three-days-ago (:end created-employment))))

  (it "returns 422 if employment record is invalid"
    (let [request (-> (request :post "/v1/employments")
                    (wring/set-user-api (test-stockroom-api))
                    (assoc :params {:person-id 1
                                    :position-id 3
                                    :location-id 2}))
          {employment-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-employment :result} (api/find-employment-by-id api employment-id)
          errors (:errors (:body response))]
      (should= 422 (:status response))
      (should-contain employment-invalid-person-id errors)
      (should-contain employment-invalid-position-id errors)
      (should-contain employment-missing-start-date errors)))

  (it "validates dates on create"
    (let [{api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api stanley-id :result} (api/create-person! api {:first-name "Stanley" :last-name "Hudson"})
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          request (-> (request :post "/v1/employments")
                    (wring/set-user-api api)
                    (assoc :params {:person-id stanley-id
                                    :position-id developer-id
                                    :location-id location-id
                                    :start "unknown"
                                    :end "unknown"}))
          {status :status body :body :as response} (handler request)]
      (should= 422 status)
      (should= {:errors [employments-malformatted-start-date employments-malformatted-end-date]}
               body)))

  (it "finds an employment by id"
    (let [{api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          {api :api employment-id :result} (api/create-employment! api {:person-id john-id
                                                                         :position-id developer-id
                                                                         :location-id location-id
                                                                         :start ten-days-ago
                                                                         :end four-days-ago})
          {employment :result} (api/find-employment-by-id api employment-id)
          request (-> (request :get (format "/v1/employments/%s" employment-id))
                    (wring/set-user-api api))
          response (handler request)
          body (:body response)]
      (should= 200 (:status response))
      (should= (:employment-id body) (:employment-id employment))
      (should= (:position-id body) (:position-id employment))
      (should= (:start body) (format-date-for-web (:start employment)))
      (should= (:end body) (format-date-for-web (:end employment)))))

  (it "updates an employment"
    (let [{api :api developer-id :result} (api/create-employment-position! @api {:name "Developer"})
          {api :api john-id :result} (api/create-person! api {:first-name "John" :last-name "Smith"})
          {api :api location-id :result} (api/create-location! api {:name "Chicago"})
          {api :api employment-id :result} (api/create-employment! api {:person-id john-id
                                                                        :position-id developer-id
                                                                        :location-id location-id
                                                                        :start ten-days-ago
                                                                        :end four-days-ago})
          request (-> (request :put (format "/v1/employments/%s" employment-id))
                    (wring/set-user-api api)
                    (assoc :params {:person-id john-id
                                    :position-id developer-id
                                    :start (format-date-for-web five-days-ago)
                                    :end (format-date-for-web ten-days-ago)}))
          response (handler request)
          {updated-employment :result} (api/find-employment-by-id api employment-id)]
      (should= 200 (:status response))
      (should= john-id (:person-id updated-employment)) 
      (should= developer-id (:position-id updated-employment))
      (should= ten-days-ago (:start updated-employment))
      (should= four-days-ago (:end updated-employment))))
)
