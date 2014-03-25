(ns stockroom.api.middleware.authentication-spec
  (:require [speclj.core :as user :refer :all]
            [clojure.core.cache :as cache]
            [stockroom.api.middleware.authentication :refer :all]
            [stockroom.api.open-id-token :as open-id-token]
            [stockroom.api.spec-helper :refer [test-stockroom-api]]
            [stockroom.api.util.request :as api-request]
            [stockroom.api.util.response :as api-response]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn valid-google-id-token []
  {:header {:alg "RS256"
            :kid "f78e97530f2b0909de41052d2434a104095aa8d7"}
   :claims {:sub "100258026714845058316"
            :email_verified "true"
            :iss "accounts.google.com"
            :aud "625778872813-cm7ao1eo65pci602tcvjr6732uqm58ta.apps.googleusercontent.com"
            :email "myles@8thlight.com"}
   :signature ""})

(defn with-auth-token [request id-token]
  (let [encoded-token (if (string? id-token) id-token (open-id-token/encode-id-token id-token))]
    (assoc-in request [:headers "authorization"] (format "Token %s" encoded-token))))

(defmacro should-respond-with-error [response error]
  `(do
     (should= 401 (:status ~response))
     (should= {:errors [~error]} (:body ~response))))

(defn test-handler
  ([] (test-handler {}))
  ([options]
   (wrap-authenticate-with-id-token
     identity (merge {:token-handler #(try
                                       (open-id-token/decode-id-token %)
                                       (catch Exception e
                                         nil))}
                     options))))

(describe "stockroom.api.middleware.authentication"
  (with-stubs)

  (context "wrap-authenticate-with-id-token"

    (it "responds with a missing id token error if there is no authentication header"
       (let [handler (test-handler)
             response (handler {})]
         (should-respond-with-error response api-response/missing-id-token-error)))

    (it "responds with an missing id token error if the header is present with no token"
      (let [handler (test-handler)
            request (with-auth-token {} "")
            response (handler request)]
        (should-respond-with-error response api-response/missing-id-token-error)))

  )

  (context "access-with-token"
    (with api (test-stockroom-api))

    (it "sets the user on the request"
      (let [id-token (valid-google-id-token)
            uid (open-id-token/subject id-token)
            {api :api user-id :result} (api/create-user-with-authentication! @api {:uid uid :provider :google})
            {api :api user :result} (api/find-user-by-id api user-id)
            request (-> {} (wring/set-service-api api))
            handler #(identity %)]
        (with-redefs [parse-and-verify-token (fn [_] id-token)]
          (let [modified-request (access-with-token handler request id-token)]
            (should= user (api-request/current-user modified-request))))))

    (it "responds with an invalid id if the token handler returns nil"
      (let [handler #(identity %)
            request (-> {} (wring/set-service-api @api))
            token "abc"]
        (with-redefs [parse-and-verify-token (fn [_] nil)]
          (let [response (access-with-token handler request token)]
            (should-respond-with-error response api-response/invalid-id-token-error)))))

    (it "responds with user not found if the token handler returns nil"
      (let [id-token (valid-google-id-token)
            uid (open-id-token/subject id-token)
            request (-> {} (wring/set-service-api @api))
            handler #(identity %)]
        (with-redefs [parse-and-verify-token (fn [_] id-token)]
          (let [response (access-with-token handler request "token")]
            (should-respond-with-error response api-response/user-not-found-error)))))

    )

  (context "access-with-bearer"
    (with api (test-stockroom-api))

    (it "sets the user on the request"
      (let [id-token (valid-google-id-token)
            uid (open-id-token/subject id-token)
            {api :api user-id :result} (api/create-user-with-authentication! @api {:uid uid :provider :google})
            {api :api user :result} (api/find-user-by-id api user-id)
            request (-> {} (wring/set-service-api api))
            handler #(identity %)]
        (with-redefs [get-google-id-from-access-token-with-cache (fn [_] uid)]
          (let [modified-request (access-with-bearer handler request "token")]
            (should= user (api-request/current-user modified-request))))))

    (it "responds with an invalid id if the user is not found"
      (let [handler #(identity %)
            request (-> {} (wring/set-service-api @api))
            token "abc"]
        (with-redefs [get-google-id-from-access-token-with-cache (fn [_] nil)]
            (let [response (access-with-bearer handler request token)]
              (should-respond-with-error response api-response/user-not-found-error)))))

  )

  (context "token-from-request"

    (it "auth-header gets the token with a valid header"
      (let [token-header "Token the-token"
            bearer-header "Bearer the-token"]
        (should= ["Token" "the-token"] (token-from-auth-header token-header))
        (should= ["Bearer" "the-token"] (token-from-auth-header bearer-header))))

    (it "auth-header trims extra white space"
      (let [token-header " Token    the-token "
            bearer-header " Bearer    the-token "]
        (should= ["Token" "the-token"] (token-from-auth-header token-header))
        (should= ["Bearer" "the-token"] (token-from-auth-header bearer-header))))

    (it "auth-header the token can be in quotes"
      (let [token-header "Token \"the-token\""
            bearer-header "Bearer \"the-token\""]
        (should= ["Token" "the-token"] (token-from-auth-header token-header))
        (should= ["Bearer" "the-token"] (token-from-auth-header bearer-header))))

    (it "returns nil when the first part of the header is not token or bearer"
      (should-be-nil (token-from-auth-header "first-part the-token")))

    (it "returns nil when there are not exactly two parts"
      (should-be-nil (token-from-auth-header "first-part the-token third"))
      (should-be-nil (token-from-auth-header "Token")))

    )

  (context "get-google-id-from-access-token"
    (it "completes a request cycle"
      (let [access-token "access-token"
            url (atom "")
            fake-response {:status 200 :body "{\"user_id\":\"some-token\"}"}
            fake-get (fn [given-url _] do (reset! url given-url) fake-response)]
        (should= "some-token" (get-google-id-from-access-token "access-token" fake-get))
        (should= (str "https://www.googleapis.com/oauth2/v2/tokeninfo?access_token=" access-token) @url)))

    (it "returns nil if the status is not 200"
      (let [fake-response {:status 500 :body "NOT JSON"}
            fake-get (fn [_ _] fake-response)]
        (should= nil (get-google-id-from-access-token "access-token" fake-get))))
    )

  (context "get-google-id-from-access-token-with-cache"
    (it "reads the value from the cache if it's available"
      (let [access-token "access-token"
            id "id"
            cache (atom (cache/basic-cache-factory {access-token id}))]
        (should= id (get-google-id-from-access-token-with-cache access-token cache))))

    (it "returns the value from the service call if there is not a cache entry"
      (with-redefs [get-google-id-from-access-token (fn [_] "id")]
        (let [cache (atom (cache/basic-cache-factory {}))]
          (should= "id" (get-google-id-from-access-token-with-cache "access-token" )))))

    (it "persists a new value to cache after a cache miss"
      (let [cache (atom (cache/basic-cache-factory {}))]
        (with-redefs [get-google-id-from-access-token (fn [_] "id")]
          (get-google-id-from-access-token-with-cache "access-token" cache)
          (let [entry (cache/lookup @cache "access-token")]
            (should= "id" entry)))))

    (it "does not persist a nil value after a cache miss"
      (let [cache (atom (cache/basic-cache-factory {}))]
        (with-redefs [get-google-id-from-access-token (fn [_] nil)]
          (get-google-id-from-access-token-with-cache "access-token" cache)
          (let [entry (cache/lookup @cache "access-token")]
            (should= nil entry)))))

    )

  (context "parse-and-verify-token"

    (it "returns the parsed token if the verify returns true"
      (let [id-token (valid-google-id-token)
            raw-id-token (open-id-token/encode-id-token id-token)]
        (should= id-token
                 (parse-and-verify-token raw-id-token (fn [token] true)))))

    (it "returns nil if the verifier returns false"
      (let [id-token (valid-google-id-token)
            raw-id-token (open-id-token/encode-id-token id-token)]
        (should-be-nil (parse-and-verify-token raw-id-token (fn [token] false)))))

    (it "returns nil if it cant parse the token"
      (should-be-nil (parse-and-verify-token "abc" (fn [] true))))

    )

  )
