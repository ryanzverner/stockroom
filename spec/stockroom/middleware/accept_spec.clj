(ns stockroom.middleware.accept-spec
  (:require [speclj.core :refer :all]
            [stockroom.middleware.accept :refer [wrap-accept]]))

(describe "stockroom.middleware.accept"

  (with-stubs)

  (it "calls the fallback handler when the accept header does not match"
    (let [request {:headers {"accept" "application/edn"}}
          handler (stub :handler {:return {:status 200}})
          fallback-handler (stub :fallback-handler {:return {:status 200}})
          response ((wrap-accept handler fallback-handler "application" "json") request)]
      (should-have-invoked :fallback-handler {:with [request]})))

  (it "calls the handler when the first item in the accept header matches"
    (let [request {:headers {"accept" "application/edn"}}
          handler (stub :handler {:return {:status 200}})
          fallback-handler (stub :fallback-handler {:return {:status 200}})
          response ((wrap-accept handler fallback-handler "application" "edn") request)]
      (should-have-invoked :handler {:with [request]})))

  (it "calls the handler when the second item in the accept header matches"
    (let [request {:headers {"accept" "application/json,application/edn"}}
          handler (stub :handler {:return {:status 200}})
          fallback-handler (stub :fallback-handler {:return {:status 200}})
          response ((wrap-accept handler fallback-handler "application" "edn") request)]
      (should-have-invoked :handler {:with [request]})))

  (it "uses the content-type when the accept header is not present"
    (let [request {:content-type "application/edn"}
          handler (stub :handler {:return {:status 200}})
          fallback-handler (stub :fallback-handler {:return {:status 200}})
          response ((wrap-accept handler fallback-handler "application" "edn") request)]
      (should-have-invoked :handler {:with [request]})))

  )
