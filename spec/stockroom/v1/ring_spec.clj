(ns stockroom.v1.ring-spec
  (:require [speclj.core :refer :all]
            [stockroom.spec-helper :refer :all]
            [stockroom.v1.authorized-api :refer [wrap-with-authorized-api]]
            [stockroom.v1.ring :refer :all]))

(describe "stockroom.v1.ring"

  (context "wrap-api"

    (it "creates a user-api and a service-api"
      (let [handler (wrap-api identity :base-api)
            request (handler {})]
        (should= :base-api (user-api request))
        (should= :base-api (service-api request))))

    )

  (context "wrap-mysql-api"

    (it "creates a mysql user-api and a mysql service-api"
      (let [mysql-api (v1-mysql-api)
            db-spec @test-db-spec
            handler (wrap-mysql-api identity db-spec)
            request (handler {})
            mysql-api-type (class mysql-api)]
        (should-be-a mysql-api-type (user-api request))
        (should-be-a mysql-api-type (service-api request))))

    )

  (context "wrap-authorized-user-api"

    (it "wraps the user api impl with the authorized impl"
      (let [mysql-api (v1-mysql-api)
            db-spec @test-db-spec
            handler (-> identity
                      (wrap-authorized-user-api {:current-user-id-from-request (fn [request] 10)})
                      (wrap-mysql-api db-spec))
            request (handler {})
            mysql-api-type (class mysql-api)
            authorized-api-type (class (wrap-with-authorized-api nil 10))]
        (should-be-a authorized-api-type (user-api request))
        (should-be-a mysql-api-type (service-api request))))

    )

  (it "puts the user-api into the request"
    (should= {:stockroom/user-api :user-api}
             (set-user-api {} :user-api)))

  (it "gets the user-api from the request"
    (should= :user-api
             (user-api {:stockroom/user-api :user-api})))

  (it "puts the service-api into the request"
    (should= {:stockroom/service-api :service-api}
             (set-service-api {} :service-api)))

  (it "gets the service-api from the request"
    (should= :service-api
             (service-api {:stockroom/service-api :service-api})))

  )
