(ns stockroom.api.v1.me-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.util.request :as api-request]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "/v1/me/permissions"

  (with api (test-stockroom-api))

  (it "lists the current user's permissions"
    (let [permission1 "I can do this"
          permission2 "I can do that"
          {api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "1001" :name "John Smith"})
          {api :api user :result} (api/find-user-by-id api user-id)
          {api :api group-id :result} (api/create-permissions-group! api {:name "my-group"})
          {api :api} (api/add-user-to-group! api {:user-id user-id :group-id group-id})
          {api :api} (api/add-permission-to-group! api {:group-id group-id :permission permission1})
          {api :api} (api/add-permission-to-group! api {:group-id group-id :permission permission2})
          request (-> (request :get "/v1/me/permissions")
                      (wring/set-user-api api)
                      (api-request/set-current-user user))
          response (handler request)]
      (should= 200 (:status response))
      (should== [permission1 permission2] (:body response)))))
