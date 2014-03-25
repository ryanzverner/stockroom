(ns stockroom.admin.auth.routes-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.auth.login-view :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.auth.routes :refer :all]
            [stockroom.admin.util.request :as admin-request]
            [stockroom.api.open-id-token :as open-id-token]
            [clj-oauth2.client :as oauth2]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-user-not-found [response id-token]
  `(do
     (should= 401 (:status ~response))
     (should= (render-user-not-found ~id-token)
              (:body ~response))))

(describe "stockroom.admin.auth.routes"

  (with-stubs)

  (with ctx (test-admin-context))
  (with auth (handler @ctx))
  (with api (test-stockroom-api))

  (it "renders the login page"
    (let [request (request :get (urls/login-url @ctx {}))
          response (@auth request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "logs the user out"
    (let [request (request :get (urls/logout-url @ctx))
          response (@auth request)]
      (should= 302 (:status response))
      (should= (urls/root-url @ctx)
               (get-in response [:headers "Location"]))
      (should-be-nil (:session response))
      (should-contain :session response)))

  (it "builds view data for login view"
    (let [return-url "http://example.com"
          request (-> (request :get "")
                    (assoc-in [:params :return-url] return-url))]
      (should= {:login-url (urls/google-oauth2-login-url @ctx {:return-url return-url})}
               (build-view-data-for-login-view {:context @ctx
                                                :request request}))))

  (context "google oauth2 login"

    (it "redirects to google for authentication"
      (let [oauth-config (google-oauth2-config @ctx)
            return-url "http://return.url"
            request (-> (request :get (urls/google-oauth2-login-url @ctx))
                      (assoc-in [:params :return-url] return-url))
            response (@auth request)]

        (should-redirect-to response (:uri (oauth2/make-auth-request oauth-config)))
        (should= return-url (-> response :flash :return-url))))

    )

  (context "google oauth2 callback"

    (with uid "115597353601993464222")

    (with valid-access-token-response {:access-token "ya29.AHES6ZRpSTXmYDuPaFXJuZ585qrJEymzuIEgw8-E4hPazqrPrMUjO7jQ"
                                       :token-type "Bearer"
                                       :query-param :access_token,
                                       :params {:expires_in 3599,
                                                :id_token "eyJhbGciOiJSUzI1NiIsImtpZCI6IjJkNWQ0YTRkMWExYjBjYjM1MmRkOTRmMWNhYmEyNGYzOWNlY2ZiMTQifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwidG9rZW5faGFzaCI6IkF1Q2MyQ3pDdlFQa25sZFhaU2tOTFEiLCJhdF9oYXNoIjoiQXVDYzJDekN2UVBrbmxkWFpTa05MUSIsInZlcmlmaWVkX2VtYWlsIjoidHJ1ZSIsImVtYWlsX3ZlcmlmaWVkIjoidHJ1ZSIsImhkIjoiOHRobGlnaHQuY29tIiwiZW1haWwiOiJtaWNhaEA4dGhsaWdodC5jb20iLCJhdWQiOiI1NjEyNzU3MTM1MTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJjaWQiOiI1NjEyNzU3MTM1MTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhenAiOiI1NjEyNzU3MTM1MTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJpZCI6IjExNTU5NzM1MzYwMTk5MzQ2NDIyMiIsInN1YiI6IjExNTU5NzM1MzYwMTk5MzQ2NDIyMiIsImlhdCI6MTM2NTAyMDQ1MywiZXhwIjoxMzY1MDI0MzUzfQ.azhxY1nYQdhZvdaWLvZQCYMZ2DGTdmMU9cbRW9_bLOku2G1S81qOfCdsPvS3H5vNCHhxzWWO8eJvnAOFURi43Wn4ABMUm6-7J_GrZwc75WYN9r261dwPuTDaB3ZPReRlTRHhhpCF4CxEMT0DaZ738e1Kh10tL7a21wCZ9OWmAE4"
                                                }})
    (around [it]
      (with-redefs [oauth2/get-access-token (fn [& args] @valid-access-token-response)]
        (it)))

    (it "puts the uid and provider in the session if the user exists"
      (let [auth-data {:provider :google :uid @uid}
            {api :api user-id :result} (api/create-user-with-authentication! @api auth-data)
            return-url "http://return.url"
            request (-> (request :get (urls/google-oauth2-callback-url @ctx))
                      (wring/set-service-api api)
                      (assoc-in [:flash :return-url] return-url))
            response (@auth request)]
        (should= {:provider "google" :uid @uid}
                 (admin-request/current-uid-and-provider response))
        (should-redirect-to response return-url)))

    (it "puts the uid and provider in the session when the user does not exist"
      (let [id-token (open-id-token/decode-id-token (-> @valid-access-token-response :params :id_token))
            return-url "http://return.url"
            request (-> (request :get (urls/google-oauth2-callback-url @ctx))
                      (wring/set-service-api @api)
                      (assoc-in [:flash :return-url] return-url))
            response (@auth request)]
        (should= {:provider "google" :uid @uid}
                 (admin-request/current-uid-and-provider response))))

    )

  )
