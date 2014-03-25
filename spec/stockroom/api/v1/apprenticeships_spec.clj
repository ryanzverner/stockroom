(ns stockroom.api.v1.apprenticeships-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def twelve-days-ago (days-ago-at-midnight 12))
(def four-days-ago (days-ago-at-midnight 4))
(def three-days-ago (days-ago-at-midnight 3))
(def one-day-ago (days-ago-at-midnight 1))
(def one-day-from-now (days-from-now-at-midnight 1))

(def mentor
  {:first-name "Jane"
   :last-name "Doe"
   :email "jane.doe@example.com"})

(def mentee
  {:first-name "John"
   :last-name "Doe"
   :email "john.doe@example.com"})

(defn apprenticeship [person-id mentor-id]
  {:person-id person-id
   :start twelve-days-ago
   :end one-day-ago
   :skill-level "resident"
   :mentorships [{:person-id mentor-id
                  :start four-days-ago
                  :end three-days-ago}]})

(describe "/v1/apprenticeships"

  (with api (test-stockroom-api))

  (it "finds all apprenticeships"
    (let [{api :api person-id :result} (api/create-person! @api mentee)
          {api :api mentor-id :result} (api/create-person! api mentor)
          {api :api apprenticeship-id1 :result } (api/create-apprenticeship! api (apprenticeship person-id mentor-id))
          {api :api apprenticeship-id2 :result } (api/create-apprenticeship! api (apprenticeship person-id mentor-id))
          {api :api apprenticeship1 :result} (api/find-apprenticeship-by-id api apprenticeship-id1)
          {api :api apprenticeship2 :result} (api/find-apprenticeship-by-id api apprenticeship-id2)
          {api :api apprenticeships :result status :status} (api/find-all-apprenticeships api)
          request (-> (request :get "/v1/apprenticeships")
                      (wring/set-user-api api))
          response (handler request)
          response-apprenticeships (:apprenticeships (:body response))]
      (should= 200 (:status response))
      (should= 2 (count response-apprenticeships))
      (should= (:id apprenticeship1) (:id (nth response-apprenticeships 0)))
      (should= (:id apprenticeship2) (:id (nth response-apprenticeships 1)))
      (should= (:email (:person apprenticeship1)) (:email (:person (nth response-apprenticeships 0))))
      (should= (:email (:person apprenticeship2)) (:email (:person (nth response-apprenticeships 1))))))

  (it "includes the current location"
    (let [{api :api mentor-id :result} (api/create-person! @api mentor)
          {api :api apprentice-id :result} (api/create-person! api mentee)
          {api :api apprenticeship-id :result } (api/create-apprenticeship! api (apprenticeship apprentice-id mentor-id))
          {api :api location-id :result} (api/create-location! api {:name "London"})
          {api :api position-id :result} (api/create-employment-position! api {:name "resident"})
          {api :api employment-id :result} (api/create-employment! api {:person-id apprentice-id
                                                                        :position-id position-id
                                                                        :location-id location-id
                                                                        :start twelve-days-ago
                                                                        :end one-day-from-now})
          {api :api membership-id :result} (api/create-location-membership! api employment-id location-id {:start three-days-ago})
          request (-> (request :get "/v1/apprenticeships")
                      (wring/set-user-api api))
          response (handler request)
          response-apprenticeships (:apprenticeships (:body response))]
      (should= 200 (:status response))
      (should= 1 (count response-apprenticeships))
      (should= "London" (:name (:current-location (:person (first response-apprenticeships)))))))

  (it "finds an apprenticeship by id"
    (let [{api :api person-id :result} (api/create-person! @api mentee)
          {api :api mentor-id :result} (api/create-person! api mentor)
          {api :api apprenticeship-id :result } (api/create-apprenticeship! api (apprenticeship person-id mentor-id))
          {api :api apprenticeship :result} (api/find-apprenticeship-by-id api apprenticeship-id)
          request (-> (request :get (format "/v1/apprenticeships/%s" apprenticeship-id))
                      (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= apprenticeship (:body response))))

  (it "creates an apprenticeship"
    (let [{api :api person-id :result} (api/create-person! @api mentee)
          {api :api mentor-id :result} (api/create-person! api mentor)
          request (-> (request :post "/v1/apprenticeships")
                      (wring/set-user-api api)
                      (assoc :params {:person-id person-id
                                      :start (format-date-for-web four-days-ago)
                                      :end (format-date-for-web three-days-ago)
                                      :skill-level "resident"
                                      :mentorships [{:person-id mentor-id
                                                     :start (format-date-for-web four-days-ago)
                                                     :end (format-date-for-web three-days-ago)}]}))
          {apprenticeship-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-apprenticeship :result} (api/find-apprenticeship-by-id api apprenticeship-id)]
      (should= 201 (:status response))
      (should= apprenticeship-id (:body response))
      (should= person-id (:person-id created-apprenticeship))
      (should= four-days-ago (:start created-apprenticeship))
      (should= three-days-ago (:end created-apprenticeship))
      (should= "resident" (:skill-level created-apprenticeship))))

  (it "returns the current apprentice graduations by location"
      (let [api @api
            {api :api location-id :result} (api/create-location! api {:name "chicago"})
            {api :api position-id :result} (api/create-employment-position! api {:name "resident"})
            {api :api mentor-id :result} (api/create-person! api mentor)
            {api :api mentee-id :result} (api/create-person! api mentee)
            {api :api employment-one-id :result} (api/create-employment! api {:person-id mentor-id
                                                                              :position-id position-id
                                                                              :location-id location-id
                                                                              :start twelve-days-ago
                                                                              :end one-day-from-now})
            {api :api employment-two-id :result} (api/create-employment! api {:person-id mentee-id
                                                                              :position-id position-id
                                                                              :location-id location-id
                                                                              :start twelve-days-ago
                                                                              :end one-day-from-now})
            {api :api mentor-location-membership-id :result} (api/create-location-membership! api employment-one-id location-id {:start one-day-ago})
            {api :api mentee-location-membership-id :result} (api/create-location-membership! api employment-two-id location-id {:start one-day-ago})
            {api :api apprenticeship-id :result}
            (api/create-apprenticeship! api {:person-id mentee-id
                                             :start twelve-days-ago
                                             :end one-day-from-now
                                             :skill-level "resident"
                                             :mentorships [{:person-id mentor-id
                                                            :start twelve-days-ago
                                                            :end one-day-from-now}]})
            response (-> (request :get "/v1/apprenticeships/graduations")
                         (wring/set-user-api api)
                         (handler))]
        (should= 200 (:status response))
        (should= #{{:location-name "chicago"
                    :current-apprentices #{{:first-name "John"
                                            :last-name "Doe"
                                            :graduates-at one-day-from-now}}}}
                 (:body response)))))
