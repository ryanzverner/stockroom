(ns stockroom.admin.middleware.auth-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.auth.login-view :refer [render-user-not-found]]
            [stockroom.admin.middleware.auth :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.request :as admin-request]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(describe "stockroom.admin.middleware.auth"
  (with ctx (test-admin-context))
  (with api (test-stockroom-api))

  (context "wrap-require-logged-in-user"

    (it "redirects the login page when there is not user"
      (let [request {:uri "/foo/bar" :query-string "baz=quux&ohai=kthxbai"}
            handler (wrap-require-logged-in-user identity @ctx)
            response (handler request)]
        (should= 302 (:status response))
        (should= (urls/login-url @ctx {:return-url "/foo/bar?baz=quux&ohai=kthxbai"})
                 (get-in response [:headers "Location"]))))

    (it "calls the next handler when the user is logged in"
      (let [request {:admin/current-user :user}
            handler (wrap-require-logged-in-user identity @ctx)
            response (handler request)]
        (should= request (handler request))))

    (it "renders the user not found view uid and provider are present in the session but no user is found"
      (let [auth-data {:provider "google" :uid "10000"}
            request (-> (request :get "/foo/bar")
                      (wring/set-service-api @api)
                      (admin-request/set-uid-and-provider auth-data))
            response ((wrap-require-logged-in-user identity @ctx) request)]
        (should= 401 (:status response))
        (should= (render-user-not-found "google" "10000") (:body response))))

    )

  (context "wrap-load-logged-in-user"

    (it "loads the user from the session"
      (let [auth-data {:provider :google :uid "10000"}
            {api :api user-id :result} (api/create-user-with-authentication! @api auth-data)
            {api :api user :result} (api/find-user-by-id api user-id)
            request (-> (request :get "")
                      (wring/set-service-api api)
                      (admin-request/set-uid-and-provider auth-data))
            modified-request ((wrap-load-logged-in-user identity @ctx) request)]
        (should= user (admin-request/current-user modified-request))))

    (it "does nothing if the provider and uid are not in the session"
      (let [request (-> (request :get "")
                      (wring/set-service-api @api))
            modified-request ((wrap-load-logged-in-user identity @ctx) request)]
        (should= request modified-request)))

    (it "does nothing when the uid and provider are present in the session but no user is found"
      (let [auth-data {:provider :google :uid "10000"}
            request (-> (request :get "/foo/bar")
                      (wring/set-service-api @api)
                      (admin-request/set-uid-and-provider auth-data))
            modified-request ((wrap-load-logged-in-user identity @ctx) request)]
        (should= request modified-request)))

    )

  )
