(ns stockroom.api.open-id-token-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.open-id-token :refer :all]))

(def test-id-token "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY3OGU5NzUzMGYyYjA5MDlkZTQxMDUyZDI0MzRhMTA0MDk1YWE4ZDcifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwidmVyaWZpZWRfZW1haWwiOiJ0cnVlIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiaWQiOiIxMDAyNTgwMjY3MTQ4NDUwNTgzMTYiLCJzdWIiOiIxMDAyNTgwMjY3MTQ4NDUwNTgzMTYiLCJhdWQiOiI2MjU3Nzg4NzI4MTMtY203YW8xZW82NXBjaTYwMnRjdmpyNjczMnVxbTU4dGEuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6Im15bGVzQDh0aGxpZ2h0LmNvbSIsImNpZCI6IjYyNTc3ODg3MjgxMy1jbTdhbzFlbzY1cGNpNjAydGN2anI2NzMydXFtNTh0YS5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF6cCI6IjYyNTc3ODg3MjgxMy1jbTdhbzFlbzY1cGNpNjAydGN2anI2NzMydXFtNTh0YS5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInRva2VuX2hhc2giOiJVdEx2LUZENnlIa0cwamc1YXRRdnZnIiwiYXRfaGFzaCI6IlV0THYtRkQ2eUhrRzBqZzVhdFF2dmciLCJoZCI6Ijh0aGxpZ2h0LmNvbSIsImlhdCI6MTM5NTY4OTYwMiwiZXhwIjoxMzk1NjkzNTAyfQ.CLzxIHsTleOHd_DI_ZU4E1c5Ys9SNcGFqM0Nx4t3l0j0abnIQGwl3nIVqX9batT4yu5q2N-KRp6bvCcbgnSN5PZk0sidX4dTP2eEr2nVkKF-JSLnUu15gIEa9cPDJnib5z2TMD3OjsHg5bZgQxcB05ydVEOIZKPeZTbhKXqgwGY")
(def parsed-id-token {:header {:alg "RS256"
                               :kid "f78e97530f2b0909de41052d2434a104095aa8d7"}
                      :claims {:sub "100258026714845058316"
                               :token_hash "UtLv-FD6yHkG0jg5atQvvg"
                               :iat 1395689602
                               :email_verified "true"
                               :iss "accounts.google.com"
                               :aud "625778872813-cm7ao1eo65pci602tcvjr6732uqm58ta.apps.googleusercontent.com"
                               :cid "625778872813-cm7ao1eo65pci602tcvjr6732uqm58ta.apps.googleusercontent.com"
                               :verified_email "true"
                               :azp "625778872813-cm7ao1eo65pci602tcvjr6732uqm58ta.apps.googleusercontent.com"
                               :email "myles@8thlight.com"
                               :exp 1395693502
                               :id "100258026714845058316"
                               :hd "8thlight.com"
                               :at_hash "UtLv-FD6yHkG0jg5atQvvg"}
                      :signature "CLzxIHsTleOHd_DI_ZU4E1c5Ys9SNcGFqM0Nx4t3l0j0abnIQGwl3nIVqX9batT4yu5q2N-KRp6bvCcbgnSN5PZk0sidX4dTP2eEr2nVkKF-JSLnUu15gIEa9cPDJnib5z2TMD3OjsHg5bZgQxcB05ydVEOIZKPeZTbhKXqgwGY"})

(describe "com.eighthlight.open-id-token"

  (it "parses the raw id token"
    (should= parsed-id-token
             (decode-id-token test-id-token)))

  (it "converts an id token map to a raw id token"
    (should= parsed-id-token
             (-> test-id-token
               decode-id-token
               encode-id-token
               decode-id-token
               encode-id-token
               decode-id-token)))

  (it "gets the issuer of a parsed id token"
    (should= "accounts.google.com"
             (issuer parsed-id-token)))

  (it "gets the subject of a parsed id token"
    (should= "100258026714845058316"
             (subject parsed-id-token)))

  (it "gets the email of a parsed id token"
    (should= "myles@8thlight.com"
             (email parsed-id-token)))

  (it "gets the issued-at of a parsed id token"
    (should= 1395689602
             (issued-at parsed-id-token)))

  (it "gets the expiration of a parsed id token"
    (should= 1395693502
             (expiration parsed-id-token)))

  (it "gets the audience of a parsed id token"
    (should= "625778872813-cm7ao1eo65pci602tcvjr6732uqm58ta.apps.googleusercontent.com"
             (audience parsed-id-token)))

  )
