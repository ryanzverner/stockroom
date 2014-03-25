(ns stockroom.admin.users.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.users.new-view :refer [render-new-user-view]]
            [stockroom.admin.users.routes :refer [handler]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-new-user-view-with-errors [errors & body]
  `(let [f# (stub :render-new-user-view)]
     (with-redefs [render-new-user-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-user-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.users.routes"

  (with-stubs)
  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with users (handler @ctx))

  (it "renders the new user page"
    (let [request (request :get (urls/new-user-url @ctx))
          response (@users request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a new user"
    (let [request (-> (request :post (urls/create-user-url @ctx) {:params {:uid "10000" :name "John Smith"}})
                    (wring/set-user-api @api))
          response (@users request)
          api (wring/user-api response)
          user (:result (api/find-user-by-provider-and-uid api :google "10000"))
          user-id (:id user)
          providers (:result (api/find-authentications-for-user api user-id))
          provider (first providers)]
      (should-redirect-after-post-to response (urls/list-users-url @ctx))
      (should= 1 (count providers))
      (should= {:provider :google :uid "10000" :user-id user-id} (select-keys provider [:provider :uid :user-id]))
      (should= "John Smith" (:name user))
      (should= "Successfully created user." (:success (:flash response)))))

  (it "re-renders the new view if no uid is given"
    (let [request (-> (request :post (urls/create-user-url @ctx) {:params {:uid ""}})
                    (wring/set-user-api @api))
          response (should-render-new-user-view-with-errors
                     {:uid ["Please enter the Google uid of the user."]}
                     (@users request))
          api (wring/user-api response)]
      (should-be-nil (:result (api/find-user-by-provider-and-uid api :google "10000")))))

  (it "re-renders if the uid is already taken"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "10000"})
          request (-> (request :post (urls/create-user-url @ctx) {:params {:uid "10000"}})
                    (wring/set-user-api api))
          response (should-render-new-user-view-with-errors
                     {:uid ["The uid you provided is already in use."]}
                     (@users request))
          api (wring/user-api response)]
      (should= user-id (:id (:result (api/find-user-by-provider-and-uid api :google "10000"))))))

  (it "show all users"
    (let [request (-> (request :get (urls/list-users-url @ctx))
                    (wring/set-user-api @api))
          response (@users request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "shows a user"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "10000"})
          request (-> (request :get (urls/show-user-url @ctx {:user-id user-id}))
                    (wring/set-user-api api))
          response (@users request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "responds with not found when the user does not exist"
    (let [request (-> (request :get (urls/show-user-url @ctx {:user-id "10"}))
                    (wring/set-user-api @api))
          response (@users request)]
      (should-render-not-found response)))

  )
