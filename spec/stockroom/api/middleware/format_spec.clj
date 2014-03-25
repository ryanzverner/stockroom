(ns stockroom.api.middleware.format-spec
  (:require [cheshire.core :as json]
            [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.api.middleware.format :refer [to-json
                                                     wrap-format]])
  (:import (java.io ByteArrayInputStream)))

(describe "stockroom.api.middleware.format"

  (with-stubs)
  (with test-data {:some-thing-cool {:b {:c 10 :d 10} :d [1 2 3]}})
  (with next-handler (stub :handler {:return {:status 200 :body @test-data}}))
  (with fallback-handler (stub :fallback-handler {:return {:status 200}}))
  (with handler (wrap-format @next-handler @fallback-handler))

  (it "calls the fallback handler when the Accept is not application/json"
    (let [request {:headers {"accept" "text/html"}}
          response (@handler request)]
      (should= 200 (:status response))
      (should-have-invoked :fallback-handler {:with [request]})))

  (it "outputs camelCased json responses when Accept is application/json"
    (let [request {:headers {"accept" "application/json"}}
          response (@handler request)]
      (should= 200 (:status response))
      (should= {"Content-Length" "51", "Content-Type" "application/json; charset=utf-8"}
               (:headers response))
      (should= {"someThingCool" {"b" {"c" 10 "d" 10} "d" [1 2 3]}}
               (json/parse-string (slurp (:body response))))))

  (it "parses json params when Content-Type is application/json"
    (let [request-body (ByteArrayInputStream. (.getBytes (to-json @test-data)))
          request {:content-type "application/json"
                   :body request-body}
          response (@handler request)
          modified-request (first (first-invocation-of :handler))]
      (should= @test-data (:params modified-request))))

  )
