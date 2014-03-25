(ns stockroom.admin.clients.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.clients.routes :refer [handler]]
            [stockroom.admin.clients.new-view :refer [render-new-client-view]]
            [stockroom.admin.clients.edit-view :refer [render-edit-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-new-client-view-with-errors [errors & body]
  `(let [f# (stub :render-new-client-view)]
     (with-redefs [render-new-client-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-client-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(defmacro should-render-edit-client-view-with-errors [errors & body]
  `(let [f# (stub :render-edit-client-view)]
     (with-redefs [render-edit-client-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-edit-client-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.clients.routes"

  (with-stubs)
  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with clients (handler @ctx))

  (it "renders the new clients page"
    (let [request (request :get (urls/new-client-url @ctx))
          response (@clients request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the show client page"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :get (urls/show-client-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))
          response (@clients request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders not found when the client does not exist"
    (let [request (-> (request :get (urls/show-client-url @ctx {:client-id "adf"}))
                    (wring/set-user-api @api))
          response (@clients request)]
      (should-render-not-found response)))

  (it "renders the edit client page"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :get (urls/edit-client-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))
          response (@clients request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "responds with not found when the client does not exist"
    (let [request (-> (request :get (urls/edit-client-url @ctx {:client-id "10"})
                               {:params {:name "test1"}})
                    (wring/set-user-api @api))
          response (@clients request)]
      (should-render-not-found response)))

  (it "renders the clients index"
    (let [request (-> (request :get (urls/list-clients-url @ctx))
                    (wring/set-user-api @api))
          response (@clients request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a client"
    (let [request (-> (request :post (urls/create-client-url @ctx)
                               {:params {:name "Client Name"}})
                    (wring/set-user-api @api))
          response (@clients request)
          api (wring/user-api response)
          {users :result} (api/find-all-clients api)
          created-user (first users)]
      (should= 1 (count users))
      (should= "Client Name" (:name created-user))
      (should-redirect-after-post-to response (urls/list-clients-url @ctx))
      (should= "Successfully created client."
               (-> response :flash :success))))

  (it "re-renders the new client page when validation errors"
    (let [request (-> (request :post (urls/create-client-url @ctx))
                    (wring/set-user-api @api))
          response (should-render-new-client-view-with-errors
                     {:name ["Please enter a name."]}
                     (@clients request))]
      (should= 0 (count (:result (api/find-all-clients (wring/user-api response)))))))

  (it "updates a client"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :put (urls/update-client-url @ctx {:client-id client-id})
                               {:params {:name "new Client Name"}})
                    (wring/set-user-api api))
          response (@clients request)
          api (wring/user-api response)
          {updated-user :result} (api/find-client-by-id api client-id)]
      (should= "new Client Name" (:name updated-user))
      (should-redirect-after-post-to response (urls/list-clients-url @ctx))
      (should= "Successfully updated client."
               (-> response :flash :success))))

  (it "rerenders the edit form when validation errors"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :put (urls/update-client-url @ctx {:client-id client-id})
                               {:params {:name ""}})
                    (wring/set-user-api api))
          response (should-render-edit-client-view-with-errors
                     {:name ["Please enter a name."]}
                     (@clients request))
          api (wring/user-api response)
          {updated-user :result} (api/find-client-by-id api client-id)]
      (should= "test" (:name updated-user))))

  )
