(ns stockroom.admin.auth.routes
  (:require [clj-oauth2.client :as oauth2]
            [compojure.core :refer [ANY GET routes]]
            [ring.util.response :as response]
            [stockroom.admin.auth.login-view :refer :all]
            [stockroom.admin.context :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.request :as admin-request]
            [stockroom.admin.util.response :as admin-response :refer [when-status]]
            [stockroom.api.open-id-token :as open-id-token]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-login-view [{:keys [context request]}]
  (let [return-url (-> request :params :return-url)
        url-params (if return-url {:return-url return-url} {})]
    {:login-url (urls/google-oauth2-login-url context url-params)}))

(defn show-login [context request]
  (-> {:request request :context context}
    build-view-data-for-login-view
    render-login-view
    response/response))

(defn do-logout [context request]
  (-> (response/redirect (urls/root-url context))
    admin-response/reset-session))

(defn return-url-from-request [request]
  (-> request :flash :return-url))

(defn store-return-url [response request]
  (let [return-url (-> request :params :return-url)]
    (assoc-in response [:flash :return-url] return-url)))

(defn google-oauth2-login [context request oauth2-params]
  (-> oauth2-params
    oauth2/make-auth-request
    :uri
    response/redirect
    (store-return-url request)))

(defn get-access-token [oauth2-params request-params]
  (oauth2/get-access-token
    oauth2-params
    request-params
    (:uri (oauth2/make-auth-request oauth2-params))))

(defn google-oauth2-callback [context request oauth2-params]
  (let [access-token (get-access-token oauth2-params (:params request))
        raw-id-token (-> access-token :params :id_token)
        id-token (open-id-token/decode-id-token raw-id-token)
        uid (open-id-token/subject id-token)]
    (-> (return-url-from-request request)
      response/redirect
      (admin-request/set-uid-and-provider {:uid uid :provider :google}))))

(defn google-oauth2-config [ctx]
  {:access-token-uri "https://accounts.google.com/o/oauth2/token"
   :access-type "online"
   :authorization-uri "https://accounts.google.com/o/oauth2/auth"
   :client-id (google-oauth2-client-id ctx)
   :client-secret (google-oauth2-client-secret ctx)
   :force-https false
   :grant-type "authorization_code"
   :redirect-uri (urls/google-oauth2-callback-url ctx)
   :scope ["https://www.googleapis.com/auth/userinfo.email"]
   :trace-messages false})

(defn handler [ctx]
  (let [oauth2-params (google-oauth2-config ctx)]
    (routes
      (GET "/login"                       request (show-login ctx request))
      (ANY "/logout"                      request (do-logout ctx request))
      (GET "/auth/google_oauth2"          request (google-oauth2-login ctx request oauth2-params))
      (GET "/auth/google_oauth2/callback" request (google-oauth2-callback ctx request oauth2-params)))))
