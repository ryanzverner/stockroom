(ns stockroom.admin.util.response-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer :all]))

(describe "stockroom.admin.util.response"

  (with ctx (test-admin-context))

  (context "redirect-to-login-url"

    (it "builds a redirect to the login-url given the current request"
      (let [request {:uri "/foo/bar" :query-string "baz=quux&ohai=kthxbai"}
            response (redirect-to-login-url @ctx request)]
        (should= 302 (:status response))
        (should= (urls/login-url @ctx {:return-url "/foo/bar?baz=quux&ohai=kthxbai"})
                 (get-in response [:headers "Location"]))))

    (it "builds a redirect to the login-url given the current request when there is no query-string"
      (let [request {:uri "/foo/bar"}
            response (redirect-to-login-url @ctx request)]
        (should= 302 (:status response))
        (should= (urls/login-url @ctx {:return-url "/foo/bar"})
                 (get-in response [:headers "Location"]))))

    )

  )
