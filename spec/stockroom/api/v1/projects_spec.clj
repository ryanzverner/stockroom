(ns stockroom.api.v1.projects-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.request :as api-request]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.spec-helper :refer :all]
            [stockroom.util.time :refer [days-ago-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def two-days-ago (days-ago-at-midnight 2))
(def one-day-ago (days-ago-at-midnight 1))

(describe "/v1/projects"

  (with api (test-stockroom-api))

  (it "finds all projects by updated-at desc (most recent first)"
    (let [{api :api client-id :result} (api/create-client! @api {:name "client name"})
          project-1-created-at two-days-ago
          {api :api project-id1 :result} (do-at
                                           project-1-created-at
                                           (api/create-project! api {:name "project name1"
                                                                     :source-url "http://project1.com"
                                                                     :client-id client-id}))
          project-2-created-at one-day-ago
          {api :api project1 :result} (api/find-project-by-id api project-id1)
          {api :api project-id2 :result} (do-at
                                           project-2-created-at
                                           (api/create-project! api {:name "project name2"
                                                                     :source-url "http://project2.com"
                                                                     :client-id client-id}))
          {api :api project2 :result} (api/find-project-by-id api project-id2)
          request (-> (request :get "/v1/projects")
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= {:projects [{:id project-id2
                            :name "project name2"
                            :source-url "http://project2.com"
                            :client-id client-id
                            :created-at (:created-at project2)
                            :updated-at (:updated-at project2)}
                           {:id project-id1
                            :name "project name1"
                            :source-url "http://project1.com"
                            :client-id client-id
                            :created-at (:created-at project1)
                            :updated-at (:updated-at project1)}]}
               (:body response))))

  (it "responds with a 401 when :unauthorized"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "1000"})
          {api :api user :result} (api/find-user-by-id api user-id)
          handler (test-authorized-handler handler)
          request (-> (request :get "/v1/projects")
                    (wring/set-user-api api)
                    (api-request/set-current-user user))
          response (handler request)]
      (should-respond-with-unauthorized response)))

  (it "finds a project by id"
    (let [{api :api client-id :result} (api/create-client! @api {:name "client name"})
          project-1-created-at two-days-ago
          {api :api project-id1 :result} (do-at
                                           project-1-created-at
                                           (api/create-project! api {:name "project name1"
                                                                     :source-url "http://project.com"
                                                                     :client-id client-id}))
          {api :api project1 :result} (api/find-project-by-id api project-id1)
          request (-> (request :get (format "/v1/projects/%s" project-id1))
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= {:id project-id1
                :name "project name1"
                :source-url "http://project.com"
                :client-id client-id
                :created-at (:created-at project1)
                :updated-at (:updated-at project1)}
               (:body response))))

  (it "creates a project"
    (let [{api :api client-id :result} (api/create-client! @api {:name "client name"})
          request (-> (request :post "/v1/projects")
                      (wring/set-user-api api)
                      (assoc :params {:name "project name" :client-id client-id}))
          {project-id :body :as response} (handler request)
          api (wring/user-api response)
          {api :api project :result} (api/find-project-by-id api project-id)]
      (should= 201 (:status response))
      (should= "project name" (:name project))
      (should= client-id (:client-id project))))

  (it "updates a project"
    (let [{api :api client-id :result} (api/create-client! @api {:name "client name"})
          {api :api project-id :result} (api/create-project! api {:client-id client-id})
          request (-> (request :put (format "/v1/projects/%s" project-id))
                      (wring/set-user-api api)
                      (assoc :params {:name "test"}))
          response (handler request)
          api (wring/user-api response)
          {updated-project :result} (api/find-project-by-id api project-id)]
      (should= 200 (:status response))
      (should= "test" (:name updated-project))))

  )
