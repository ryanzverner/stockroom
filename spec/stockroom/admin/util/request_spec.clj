(ns stockroom.admin.util.request-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.util.request :refer :all]))

(describe "stockroom.admin.util.request"

  (it "sets and reads from the request current user"
    (let [user {:id 10}
          request (set-current-user {} user)]
      (should= user (current-user request))))

  (it "reads the uid and provider from the session"
    ; don't store keywords in the session, only strings
    (should= {:uid 10 :provider "google"}
             (-> {}
               (set-uid-and-provider {:uid 10 :provider :google})
               current-uid-and-provider)))

  )
