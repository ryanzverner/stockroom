(ns stockroom.admin.url-helper-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :refer :all])
  (:import (java.net URI)))

(defn test-url-context [options]
  (reify UrlContext
    (host-uri [this] (:host-uri options))
    (url-prefix [this] (:url-prefix options))))

(describe "stockroom.admin.url-helper"

  (it "builds a create user url with a host and context"
    (let [context (test-admin-context {:host-uri (URI. "https://stockroom.admin.test.com")
                                       :url-prefix "/admin-test"})]
      (should= "https://stockroom.admin.test.com/admin-test/users"
               (create-user-url context))))

  (it "builds a create user url without a host and with a context"
    (let [context (test-admin-context {:url-prefix "/admin-test"})]
      (should= "/admin-test/users"
               (create-user-url context))))

  (it "builds a create user url with a host and without a context"
    (let [context (test-admin-context {:host-uri (URI. "https://stockroom.admin.test.com")})]
      (should= "https://stockroom.admin.test.com/users"
               (create-user-url context))))

  (it "builds a create user url without a host or context"
    (should= "/users"
             (create-user-url (test-admin-context {}))))

  (it "builds a users index url"
    (should= "/admin/users"
             (list-users-url (test-admin-context {:url-prefix "/admin"}))))

  (it "builds a new user url"
    (should= "/users/new" (new-user-url (test-admin-context {}))))

  (it "builds a users url"
    (should= "/users" (list-users-url (test-admin-context {}))))

  (it "builds a google-oauth2-login-url"
    (should= "/auth/google_oauth2?return-url=http%3A%2F%2Fexample.com"
             (google-oauth2-login-url (test-admin-context {}) {:return-url "http://example.com"})))

  (it "does not add params if none given"
    (should= "/auth/google_oauth2"
             (google-oauth2-login-url (test-admin-context {}) {})))

  (it "builds a login url with return-url"
    (let [return-url "/admin?foo=1"]
      (should= "https://stockroom.admin.test.com/login?return-url=%2Fadmin%3Ffoo%3D1"
               (login-url (test-admin-context {:host-uri (URI. "https://stockroom.admin.test.com")})
                          {:return-url return-url}))))

  (it "combines a uri with a path"
    (let [expected "https://admin.test.com/login?return-url=%2Fadmin%3Ffoo%3D1"]
      (should= expected
               (build-url (URI. "https://admin.test.com")
                          "/login?return-url=%2Fadmin%3Ffoo%3D1"))
      (should= expected
               (build-url (URI. "https://admin.test.com/")
                          "/login?return-url=%2Fadmin%3Ffoo%3D1"))
      (should= expected
               (build-url (URI. "https://admin.test.com/")
                          "login?return-url=%2Fadmin%3Ffoo%3D1"))))

  (it "builds a url with a host and context"
    (let [context (test-url-context {:host-uri (URI. "https://admin.test.com")
                                     :url-prefix "/admin-test"})]
      (should= "https://admin.test.com/admin-test/users"
               (create-user-url context))))

  (it "builds a url without a host and with a context"
    (let [context (test-url-context {:url-prefix "/admin-test"})]
      (should= "/admin-test/users"
               (create-user-url context))))

  (it "builds a url with a host and without a context"
    (let [context (test-url-context {:host-uri (URI. "https://admin.test.com")})]
      (should= "https://admin.test.com/users"
               (create-user-url context))))

  (it "builds a url with an address and port"
    (let [context (test-url-context {:host-uri (URI. "http://0.0.0.0:8080")})]
      (should= "http://0.0.0.0:8080/users"
               (create-user-url context))))

  (it "builds a create user url without a host or context"
    (should= "/users"
             (create-user-url (test-url-context {}))))

  (it "builds a url with params"
    (should= "/users?return-url=http%3A%2F%2Fexample.com"
             (create-user-url (test-url-context {}) {:return-url "http://example.com"})))

  (it "does not add params if none given"
    (should= "/users"
             (create-user-url (test-url-context {}) {})))

  (it "normalizes slashes"
    (should= "https://admin.test.com/users"
               (create-user-url (test-url-context {:host-uri (URI. "https://admin.test.com")
                                                   :url-prefix "/"})))
    (should= "https://admin.test.com/users"
             (create-user-url (test-url-context {:host-uri (URI. "https://admin.test.com/")
                                                 :url-prefix "/"})))
    (should= "https://admin.test.com/users"
             (create-user-url (test-url-context {:host-uri (URI. "https://admin.test.com//")
                                                 :url-prefix "//"}))))

  )
