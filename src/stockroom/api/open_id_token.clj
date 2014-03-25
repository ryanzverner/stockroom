(ns stockroom.api.open-id-token
  (:require [clj-jwt.core :as jwt]))

(defn decode-id-token [raw-jwt]
  (into {} (jwt/str->jwt raw-jwt)))

(defn encode-id-token [jwt]
  (jwt/to-str (jwt/->JWT (:header jwt) (:claims jwt) (:signature jwt))))

;; getters adn setters

(defn issuer [jwt]
  (get-in jwt [:claims :iss]))

(defn set-issuer [jwt iss]
  (assoc-in jwt [:claims :iss] iss))

(defn subject [jwt]
  (get-in jwt [:claims :sub]))

(defn set-subject [jwt sub]
  (assoc-in jwt [:claims :sub] sub))

(defn email [jwt]
  (get-in jwt [:claims :email]))

(defn set-email [jwt email]
  (assoc-in jwt [:claims :email] email))

(defn issued-at [jwt]
  (get-in jwt [:claims :iat]))

(defn set-issued-at [jwt iat]
  (assoc-in jwt [:claims :iat] iat))

(defn expiration [jwt]
  (get-in jwt [:claims :exp]))

(defn set-expiration [jwt exp]
  (assoc-in jwt [:claims :exp] exp))

(defn audience [jwt]
  (get-in jwt [:claims :aud]))

(defn set-audience [jwt aud]
  (assoc-in jwt [:claims :aud] aud))
