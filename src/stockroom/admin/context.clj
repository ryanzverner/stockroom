(ns stockroom.admin.context
  (:require [stockroom.admin.url-helper :refer [UrlContext]]))

(defprotocol GoogleOauth2ClientContext
  (google-oauth2-client-id [this])
  (google-oauth2-client-secret [this]))

(deftype AdminApplicationContext [host-uri url-prefix google-oauth2-client-id google-oauth2-client-secret]
  UrlContext
  (host-uri                    [this] host-uri)
  (url-prefix                  [this] url-prefix)

  GoogleOauth2ClientContext
  (google-oauth2-client-id     [this] google-oauth2-client-id)
  (google-oauth2-client-secret [this] google-oauth2-client-secret))

(defn admin-context [config]
  (AdminApplicationContext. (:host-uri config)
                            (:url-prefix config)
                            (:google-oauth2-client-id config)
                            (:google-oauth2-client-secret config)))
