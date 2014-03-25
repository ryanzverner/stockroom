(ns stockroom.api.v1.skills-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "/v1/skills"

  (with api (test-stockroom-api))

  (it "finds all skills"
    (let [{api :api skill1-id :result} (api/create-skill! @api {:name "skill 1"})
          {skill1 :result} (api/find-skill-by-id api skill1-id)
          {api :api skill2-id :result} (api/create-skill! api {:name "skill 2"})
          {skill2 :result} (api/find-skill-by-id api skill2-id)
          request (-> (request :get "/v1/skills")
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should== [skill1 skill2]
                (:skills (:body response)))))

  (it "finds a skill by id"
    (let [{api :api skill-id :result} (api/create-skill! @api {:name "skill 1"})
          {skill :result} (api/find-skill-by-id api skill-id)
          request (-> (request :get (format "/v1/skills/%s" skill-id))
                    (wring/set-user-api api))
          response (handler request)]
      (should= 200 (:status response))
      (should= skill (:body response))))

  (it "creates a skill"
    (let [request (-> (request :post "/v1/skills")
                    (wring/set-user-api @api)
                    (assoc :params {:name "test"}))
          {skill-id :body :as response} (handler request)
          api (wring/user-api response)
          {created-skill :result} (api/find-skill-by-id api skill-id)]
      (should= 201 (:status response))
      (should= "test" (:name created-skill))))

  (it "updates a skill"
    (let [{api :api skill-id :result} (api/create-skill! @api {})
          request (-> (request :put (format "/v1/skills/%s" skill-id))
                    (wring/set-user-api api)
                    (assoc :params {:name "test"}))
          response (handler request)
          api (wring/user-api response)
          {updated-skill :result} (api/find-skill-by-id api skill-id)]
      (should= 200 (:status response))
      (should= "test" (:name updated-skill)))))
