(ns stockroom.api.spec-helper
  (:require [speclj.core :refer :all]
            [stockroom.api.util.request :refer [current-user-id]]
            [stockroom.api.util.response :refer [unauthorized-error]]
            [stockroom.spec-helper :refer [v1-memory-api]]
            [stockroom.v1.ring :refer [wrap-authorized-user-api]]))

(defn test-stockroom-api []
  (v1-memory-api))

(defn request
  ([method uri]
   (request method uri {}))
  ([method uri extras]
   (merge
     {:request-method method
      :uri uri}
     extras)))

(defn test-authorized-handler [handler]
  (wrap-authorized-user-api handler {:current-user-id-from-request current-user-id}))

(defmacro should-respond-with-unauthorized [response]
  `(do
     (should= 403 (:status ~response))
     (should= {:errors [unauthorized-error]}
              (:body ~response))))
