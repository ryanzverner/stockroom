(ns stockroom.api.v1.director-engagements-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.response :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.api.v1.director-engagements :refer :all]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.util.time :refer [days-ago-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def ten-days-ago (days-ago-at-midnight 10))
(def five-days-ago (days-ago-at-midnight 5))

(defn test-person [api]
  (let [{api :api person-id :result} (api/create-person! api {:first-name "a"})]
    [api person-id]))

(defn test-director-engagement [api]
  (let [[api person-id] (test-person api)
        {api :api project-id :result} (api/create-project! api {:name "test"})
        {api :api director-engagement-id :result} (api/create-director-engagement! api {:person-id person-id
                                                                                        :project-id project-id
                                                                                        :start ten-days-ago
                                                                                        :end five-days-ago})]
    [api director-engagement-id]))

(describe "/v1/director-engagements"

  (with api (test-stockroom-api))

  (context "create"
    (it "creates a director-engagement"
      (let [[api person-id] (test-person @api)
            {api :api project-id :result} (api/create-project! api {:name "test"})
            request (-> (request :post "/v1/director-engagements")
                      (wring/set-user-api api)
                      (assoc :params {:person-id person-id
                                      :project-id project-id
                                      :start (format-date-for-web ten-days-ago)
                                      :end (format-date-for-web five-days-ago)}))
            {director-engagement-id :body :as response} (handler request)
            api (wring/user-api response)
            {created-director-engagement :result} (api/find-director-engagement-by-id api director-engagement-id)]
        (should= 201 (:status response))
        (should= person-id (:person-id created-director-engagement))
        (should= project-id (:project-id created-director-engagement))
        (should= ten-days-ago (:start created-director-engagement))
        (should= five-days-ago (:end created-director-engagement))))

    (it "validates dates on create"
      (let [[api person-id] (test-person @api)
            {api :api project-id :result} (api/create-project! api {:name "test"})
            request (-> (request :post "/v1/director-engagements")
                      (wring/set-user-api api)
                      (assoc :params {:person-id person-id
                                      :project-id project-id
                                      :start "unknown"
                                      :end "unknown"}))
            {status :status body :body api :api} (handler request)]
        (should= 422 status)
        (should= {:errors [director-engagements-malformatted-start-date director-engagements-malformatted-end-date]}
                 body)))

    (it "validates the director-engagement on create"
      (let [request (-> (request :post "/v1/director-engagements")
                      (wring/set-user-api @api))
            {status :status body :body api :api} (handler request)]
        (should= 422 status)))

    (it "finds a director-engagement by id"
      (let [[api director-engagement-id] (test-director-engagement @api)
            {director-engagement :result} (api/find-director-engagement-by-id api director-engagement-id)
            request (-> (request :get (format "/v1/director-engagements/%s" director-engagement-id))
                      (wring/set-user-api api))
            response (handler request)
            body (:body response)]
        (should= 200 (:status response))
        (should= (:person-id body) (:person-id director-engagement))
        (should= (:project-id body) (:project-id director-engagement))
        (should= (:start body) (format-date-for-web (:start director-engagement)))
        (should= (:end body) (format-date-for-web (:end director-engagement))))))

  (context "update"
    (it "updates a director-engagement"
      (let [[api director-engagement-id] (test-director-engagement @api)
            request (-> (request :put (format "/v1/director-engagements/%s" director-engagement-id))
                      (wring/set-user-api api)
                      (assoc :params {:end (format-date-for-web five-days-ago)}))
            response (handler request)
            api (wring/user-api response)
            {updated-director-engagement :result} (api/find-director-engagement-by-id api director-engagement-id)]
        (should= 200 (:status response))
        (should= five-days-ago (:end updated-director-engagement))))

    (it "validates dates on update"
      (let [[api director-engagement-id] (test-director-engagement @api)
            request (-> (request :put (format "/v1/director-engagements/%s" director-engagement-id))
                      (wring/set-user-api api)
                      (assoc :params {:end "unknown" :start "unknown"}))
            response (handler request)]
        (should= 422 (:status response))
        (should= {:errors [director-engagements-malformatted-start-date director-engagements-malformatted-end-date]}
                 (:body response))))

    (it "validates the director-engagement on update"
      (let [[api director-engagement-id] (test-director-engagement @api)
            request (-> (request :put (format "/v1/director-engagements/%s" director-engagement-id))
                      (wring/set-user-api api)
                      (assoc :params {:end nil :start nil :project-id nil :person-id nil}))
            response (handler request)]
        (should= 422 (:status response))))))
