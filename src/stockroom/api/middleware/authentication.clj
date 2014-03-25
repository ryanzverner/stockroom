(ns stockroom.api.middleware.authentication
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.core.cache :as cache]
            [ring.util.response :as response]
            [stockroom.api.open-id-token :as open-id-token]
            [stockroom.api.util.request :as api-request]
            [stockroom.api.util.response :as api-response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring])
  (:import (com.google.api.client.googleapis.auth.oauth2 GoogleIdTokenVerifier)
           (com.google.api.client.http.javanet NetHttpTransport)
           (com.google.api.client.json.jackson2 JacksonFactory)))

(defn- strip-quotes [s]
  (.substring s 1 (- (.length s) 1)))

(defn- maybe-strip-quotes [s]
  (if (and (= (str (.charAt s 0)) "\"")
           (= (str (.charAt s (- (.length s) 1))) "\""))
    (strip-quotes s)
    s))

(defn token-from-auth-header [auth-header]
  (when auth-header
    (let [auth-header (clojure.string/trim auth-header)
          parts (map clojure.string/trim (clojure.string/split auth-header #"\s+"))]
      (when (and (= 2 (count parts))
                 (or (= (first parts) "Token")
                     (= (first parts) "Bearer")))
        [(first parts) (maybe-strip-quotes (second parts))]))))

(defn token-from-request [request]
  (-> request
    (get-in [:headers "authorization"])
    token-from-auth-header))

(defn unauthenticated [error]
  (-> {:errors [error]}
    response/response
    (response/status 401)))

; WARNING! calling this method will make an http request
; however, the response is cached in the verifier, so we use a singleton verifier-impl
; to make sure we are using cached results when possible
(def verifier-impl (GoogleIdTokenVerifier. (NetHttpTransport.) (JacksonFactory.)))
(defn google-id-token-verifier [raw-id-token]
  (.verify verifier-impl raw-id-token))

; WARNING! calling this method will also make an http request
; but we will be caching these for a period of time
(def bearer-cache (atom (cache/ttl-cache-factory {} :ttl 600000)))
(defn get-google-id-from-access-token
    ([access-token] (get-google-id-from-access-token access-token client/get))
    ([access-token get-fn]
      (let [response (get-fn (str "https://www.googleapis.com/oauth2/v2/tokeninfo?access_token=" access-token) {:throw-exceptions false})]
        (if (= 200 (:status response))
          (-> (:body response)
              json/parse-string
              (get "user_id"))
          nil))))
(defn get-google-id-from-access-token-with-cache
    ([access-token] (get-google-id-from-access-token-with-cache access-token bearer-cache))
    ([access-token cache]
      (let [lookup (cache/lookup @cache access-token)]
        (if lookup
          lookup
          (if-let [uid (get-google-id-from-access-token access-token)]
            (do
              (swap! cache #(cache/miss % access-token uid))
              (cache/lookup @cache access-token)))))))

(defn parse-and-verify-token
  ([raw-id-token]
   (parse-and-verify-token raw-id-token google-id-token-verifier))
  ([raw-id-token verifier]
   (try
     (when (verifier raw-id-token)
       (open-id-token/decode-id-token raw-id-token))
     (catch Exception e
       nil))))


(defn- lookup-user-and-continue [handler request api uid]
 (when-status
   :success
   (fn [api user]
     (-> request
       (api-request/set-current-user user)
       handler))
   :not-found
   (fn [api]
     (unauthenticated api-response/user-not-found-error))
   (api/find-user-by-provider-and-uid api :google uid)))


(defn access-with-token [handler request token]
  (if-let [id-token (parse-and-verify-token token)]
    (let [uid (open-id-token/subject id-token)
          api (wring/service-api request)]
      (lookup-user-and-continue handler request api uid))
    (unauthenticated api-response/invalid-id-token-error)))

(defn access-with-bearer [handler request token]
  (let [uid (get-google-id-from-access-token-with-cache token)
        api (wring/service-api request)]
    (lookup-user-and-continue handler request api uid)))

(defn wrap-authenticate-with-id-token
  ([handler] (wrap-authenticate-with-id-token handler {}))
  ([handler {:keys [token-handler bearer-handler] :or {token-handler access-with-token bearer-handler access-with-bearer}}]
   (fn [request]
     (if-let [[raw-token-type raw-token] (token-from-request request)]
       (case raw-token-type
         "Token" (token-handler handler request raw-token)
         "Bearer" (bearer-handler handler request raw-token))
       (unauthenticated api-response/missing-id-token-error)))))
