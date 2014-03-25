(ns stockroom.admin.spec-helper
  (:require [speclj.core :refer [should=]]
            [stockroom.admin.context :refer [admin-context]]
            [stockroom.admin.util.response :refer [not-found-response]]
            [stockroom.spec-helper :refer [v1-memory-api]]))

(defn request
  ([method uri]
   (request method uri {}))
  ([method uri extras]
   (merge
     {:request-method method
      :uri uri}
     extras)))

(defmacro should-redirect-after-post-to [response location]
  `(do
     (should= 303 (:status ~response))
     (should= ~location ((:headers ~response) "Location"))))

(defmacro should-redirect-to [response location]
  `(do
     (should= 302 (:status ~response))
     (should= ~location ((:headers ~response) "Location"))))

(defmacro should-render-not-found [response]
  `(let [resp# ~response]
     (should= (not-found-response (wring/user-api resp#)) resp#)))

(defn test-stockroom-api []
  (v1-memory-api))

(defn test-admin-context
  ([]
   (test-admin-context {}))
  ([options]
   (admin-context (merge
                    {:host-uri nil
                     :url-prefix nil
                     :google-oauth2-client-id "abc"
                     :google-oauth2-client-secret "123"}
                    options))))
