(ns stockroom.api.v1.engagements-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.response :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.api.v1.engagements :refer :all]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.util.time :refer [days-ago-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def ten-days-ago (days-ago-at-midnight 10))
(def five-days-ago (days-ago-at-midnight 5))
(def four-days-ago (days-ago-at-midnight 4))
(def three-days-ago (days-ago-at-midnight 3))

(defn test-employment [api location-id]
  (let [{api :api person-id :result} (api/create-person! api {:first-name "a"})
        {api :api position-id :result} (api/create-employment-position! api {:name "test1"})
        {api :api employment-id :result errors :errors} (api/create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :location-id location-id
                                                                      :start ten-days-ago
                                                                      :end five-days-ago})]
    [api employment-id]))

(defn test-engagement
  ([api {:keys [project-id location-id]}]
   (let [[api employment-id] (test-employment api location-id)
         {api :api project-id :result} (if (nil? project-id) (api/create-project! api {:name "test"}) {:api api :result project-id})
         {api :api engagement-id :result} (api/create-engagement! api {:employment-id employment-id
                                                                       :project-id project-id
                                                                       :start ten-days-ago
                                                                       :end five-days-ago})]
     [api engagement-id]))
  ([api]
   (test-engagement api {})))

(describe "/v1/engagements"

  (with api (test-stockroom-api))

  (it "creates an engagement"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api employment-id] (test-employment api location-id)
          {api :api project-id :result} (api/create-project! api {:name "test"})
          request (-> (request :post "/v1/engagements")
                    (wring/set-user-api api)
                    (assoc :params {:employment-id employment-id
                                    :project-id project-id
                                    :start (format-date-for-web ten-days-ago)
                                    :end (format-date-for-web five-days-ago)
                                    :confidence-percentage 100}))
          {engagement-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-engagement :result} (api/find-engagement-by-id api engagement-id)]
      (should= 201 (:status response))
      (should= employment-id (:employment-id created-engagement))
      (should= project-id (:project-id created-engagement))
      (should= ten-days-ago (:start created-engagement))
      (should= five-days-ago (:end created-engagement))
      (should= 100 (:confidence-percentage created-engagement))))

  (it "validates dates on create"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api employment-id] (test-employment api location-id)
          {api :api project-id :result} (api/create-project! api {:name "test"})
          request (-> (request :post "/v1/engagements")
                    (wring/set-user-api api)
                    (assoc :params {:employment-id employment-id
                                    :project-id project-id
                                    :start "unknown"
                                    :end "unknown"}))
          {status :status body :body api :api} (handler request)]
      (should= 422 status)
      (should= {:errors [engagements-malformatted-start-date engagements-malformatted-end-date]}
               body)))

  (it "validates the engagement on create"
    (let [request (-> (request :post "/v1/engagements")
                    (wring/set-user-api @api))
          {status :status body :body api :api} (handler request)]
      (should= 422 status)))

  (it "finds an engagement by id"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api engagement-id] (test-engagement api {:location-id location-id})
          {engagement :result} (api/find-engagement-by-id api engagement-id)
          request (-> (request :get (format "/v1/engagements/%s" engagement-id))
                    (wring/set-user-api api))
          response (handler request)
          body (:body response)]
      (should= 200 (:status response))
      (should= (:employment-id body) (:employment-id engagement))
      (should= (:project-id body) (:project-id engagement))
      (should= (:start body) (format-date-for-web (:start engagement)))
      (should= (:end body) (format-date-for-web (:end engagement)))))

  (it "updates an engagement"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api engagement-id] (test-engagement api {:location-id location-id})
          {api :api project-id :result} (api/create-project! api {:name "test2"})
          {api :api engagement :result} (api/find-engagement-by-id api engagement-id)
          employment-id (:employment-id engagement)
          request (-> (request :put (format "/v1/engagements/%s" engagement-id))
                    (wring/set-user-api api)
                    (assoc :params {:employment-id employment-id
                                    :project-id project-id
                                    :confidence-percentage 25
                                    :start (format-date-for-web five-days-ago)
                                    :end (format-date-for-web ten-days-ago)}))
          response (handler request)
          api (wring/user-api response)
          {updated-engagement :result} (api/find-engagement-by-id api engagement-id)]
      (should= 200 (:status response))
      (should= employment-id (:employment-id updated-engagement))
      (should= project-id (:project-id updated-engagement))
      (should= 25 (:confidence-percentage updated-engagement))
      (should= five-days-ago (:start updated-engagement))
      (should= ten-days-ago (:end updated-engagement))))

  (it "validates dates on update"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api engagement-id] (test-engagement api {:location-id location-id})
          request (-> (request :put (format "/v1/engagements/%s" engagement-id))
                    (wring/set-user-api api)
                    (assoc :params {:start "unknown"
                                    :end "unknown"}))
          response (handler request)
          api (wring/user-api response)]
      (should= 422 (:status response))
      (should= {:errors [engagements-malformatted-start-date engagements-malformatted-end-date]}
               (:body response))))

  (it "responds with not found when the date does not exist"
    (let [request (-> (request :put "/v1/engagements/10")
                    (wring/set-user-api @api)
                    (assoc :params {}))
          response (handler request)]
      (should= 404 (:status response))))

  (it "deletes an engagement"
    (let [{api :api location-id :result} (api/create-location! @api {:name "chicago"})
          [api engagement-id] (test-engagement api {:location-id location-id})
          request (-> (request :delete (format "/v1/engagements/%s" engagement-id))
                    (wring/set-user-api api))
          response (handler request)
          api (wring/user-api response)]
      (should= 200 (:status response))
      (should-be-nil (:result (api/find-engagement-by-id api engagement-id)))))

  (context "index"

    (it "lists all engagements"
      (let [{api :api project1-id :result} (api/create-project! @api {:name "project"})
            {api :api project2-id :result} (api/create-project! api {:name "project"})
            {api :api location-id :result} (api/create-location! api {:name "chicago"})
            {project1 :result} (api/find-project-by-id api project1-id)
            {project2 :result} (api/find-project-by-id api project2-id)
            [api engagement1-id] (test-engagement api {:project-id project1-id :location-id location-id})
            {engagement1 :result} (api/find-engagement-by-id api engagement1-id)
            {person1 :result} (api/find-person-by-id api (:person-id (:result (api/find-employment-by-id api (:employment-id engagement1)))))
            [api engagement2-id] (test-engagement api {:project-id project2-id :location-id location-id})
            {engagement2 :result} (api/find-engagement-by-id api engagement2-id)
            {person2 :result} (api/find-person-by-id api (:person-id (:result (api/find-employment-by-id api (:employment-id engagement2)))))
            request (-> (request :get "/v1/engagements")
                      (wring/set-user-api api))
            response (handler request)
            api (wring/user-api response)]
        (should= 200 (:status response))
        (should== [{:id engagement1-id
                    :start (format-date-for-web (:start engagement1))
                    :end   (format-date-for-web (:end engagement1))
                    :employment-id (:employment-id engagement1)
                    :project-id (:project-id engagement1)
                    :created-at (format-date-for-web (:created-at engagement1))
                    :updated-at (format-date-for-web (:updated-at engagement1))
                    :confidence-percentage 100
                    :person (assoc person1
                                   :created-at (format-date-for-web (:created-at person1))
                                   :updated-at (format-date-for-web (:updated-at person1)))
                    :project (assoc project1
                                    :created-at (format-date-for-web (:created-at project1))
                                    :updated-at (format-date-for-web (:updated-at project1)))}
                   {:id engagement2-id
                    :start (format-date-for-web (:start engagement2))
                    :end   (format-date-for-web (:end engagement2))
                    :employment-id (:employment-id engagement2)
                    :project-id (:project-id engagement2)
                    :created-at (format-date-for-web (:created-at engagement2))
                    :updated-at (format-date-for-web (:updated-at engagement2))
                    :confidence-percentage 100
                    :person (assoc person2
                                   :created-at (format-date-for-web (:created-at person2))
                                   :updated-at (format-date-for-web (:updated-at person2)))
                    :project (assoc project2
                                    :created-at (format-date-for-web (:created-at project2))
                                    :updated-at (format-date-for-web (:updated-at project2)))}]
                  (:engagements (:body response)))))

    (it "lists all engagements for a project when given a project-id query param"
      (let [{api :api project-id-1 :result} (api/create-project! @api {:name "project"})
            {api :api project-id-2 :result} (api/create-project! api {:name "other project"})
            {api :api location-id :result} (api/create-location! api {:name "chicago"})
            {project :result} (api/find-project-by-id api project-id-1)
            [api engagement1-id] (test-engagement api {:project-id project-id-1 :location-id location-id})
            {engagement1 :result} (api/find-engagement-by-id api engagement1-id)
            {person1 :result} (api/find-person-by-id api (:person-id (:result (api/find-employment-by-id api (:employment-id engagement1)))))
            [api engagement2-id] (test-engagement api {:project-id project-id-1 :location-id location-id})
            [api engagement3-id] (test-engagement api {:project-id project-id-2 :location-id location-id})
            {engagement2 :result} (api/find-engagement-by-id api engagement2-id)
            {person2 :result} (api/find-person-by-id api (:person-id (:result (api/find-employment-by-id api (:employment-id engagement2)))))
            request (-> (request :get "/v1/engagements")
                        (assoc :params {:project-id project-id-1})
                        (wring/set-user-api api))
            response (handler request)
            api (wring/user-api response)]
        (should= 200 (:status response))
        (should= 2 (count (:engagements (:body response))))
        (should-contain (assoc engagement1
                               :start (format-date-for-web (:start engagement1))
                               :end   (format-date-for-web (:end engagement1))
                               :created-at (format-date-for-web (:created-at engagement1))
                               :updated-at (format-date-for-web (:updated-at engagement1))
                               :person (assoc person1
                                              :created-at (format-date-for-web (:created-at person1))
                                              :updated-at (format-date-for-web (:updated-at person1)))
                               :project (assoc project
                                               :created-at (format-date-for-web (:created-at project))
                                               :updated-at (format-date-for-web (:updated-at project))))
                        (:engagements (:body response)))
        (should-contain (assoc engagement2
                               :start (format-date-for-web (:start engagement2))
                               :end   (format-date-for-web (:end engagement2))
                               :created-at (format-date-for-web (:created-at engagement2))
                               :updated-at (format-date-for-web (:updated-at engagement2))
                               :person (assoc person2
                                              :created-at (format-date-for-web (:created-at person2))
                                              :updated-at (format-date-for-web (:updated-at person2)))
                               :project (assoc project
                                               :created-at (format-date-for-web (:created-at project))
                                               :updated-at (format-date-for-web (:updated-at project))))
                        (:engagements (:body response)))))

    (it "returns an error if start date is provided, but not end date"
      (let [request (-> (request :get "/v1/engagements")
                        (assoc :params {:start (format-date-for-web ten-days-ago)})
                        (wring/set-user-api @api))
            response (handler request)
            api (wring/user-api response)]
        (should= 400 (:status response))
        (should= {:errors [engagements-start-end-xnor-required]} (:body response))))

    (it "returns an error if end date is provided, but not start date"
      (let [request (-> (request :get "/v1/engagements")
                        (assoc :params {:end (format-date-for-web ten-days-ago)})
                        (wring/set-user-api @api))
            response (handler request)
            api (wring/user-api response)]
        (should= 400 (:status response))
        (should= {:errors [engagements-start-end-xnor-required]} (:body response))))

    (it "returns an error if start and end dates are malformed"
      (let [request (-> (request :get "/v1/engagements")
                        (assoc :params {:start "bad-date" :end "bad-date"})
                        (wring/set-user-api @api))
            response (handler request)
            api (wring/user-api response)]
        (should= 400 (:status response))
        (should= {:errors [engagements-malformatted-start-date engagements-malformatted-end-date]}
                 (:body response))))

    (it "parses query parameters into api options"
      (should= [{:start ten-days-ago :end five-days-ago :project-id 3} []]
               (parse-index-params {:start (format-date-for-web ten-days-ago)
                                    :end (format-date-for-web five-days-ago)
                                    :project-id 3})))

    )
  )
