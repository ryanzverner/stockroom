(ns stockroom.admin.sows.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.sows.routes :refer [handler]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.sows.new-view :refer [render-new-sow-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def two-days-ago (days-ago-at-midnight 2))
(def one-day-from-now (days-from-now-at-midnight 1))
(def two-days-from-now (days-from-now-at-midnight 2))
(def five-days-from-now (days-from-now-at-midnight 5))

(defmacro should-render-new-sow-view-with-errors [errors & body]
  `(let [f# (stub :render-new-sow-view)]
     (with-redefs [render-new-sow-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-sow-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.list-sows-url.routes"

  (with-stubs)

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with sows (handler @ctx))

  (it "renders the new page"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :get (urls/new-sow-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))
          response (@sows request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a sow"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          {api :api project-id :result} (api/create-project! api {:name "project 1"})
          request (-> (request :post
                               (urls/create-sow-url @ctx {:client-id client-id})
                               {:params {:start          (date-for-input one-day-from-now)
                                         :end            (date-for-input five-days-from-now)
                                         :hourly-rate    175
                                         :currency-code  "GBP"
                                         :url            "www.example.com/1"
                                         :signed-date    (date-for-input two-days-ago)
                                         :projects       [project-id]}})
                    (wring/set-user-api api))
          response (@sows request)
          api (wring/user-api response)
          created-sow (first (:result (api/find-all-sows api {})))]
      (should= one-day-from-now (:start created-sow))
      (should= five-days-from-now (:end created-sow))
      (should= 175 (:hourly-rate created-sow))
      (should= "GBP" (:currency-code created-sow))
      (should= "www.example.com/1" (:url created-sow))
      (should= two-days-ago (:signed-date created-sow))
      (should-redirect-after-post-to response (urls/show-client-url @ctx {:client-id client-id}))
      (should= "Successfully created SOW."
               (-> response :flash :success))))

  (it "re-renders the new view when validation errors"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :post (urls/create-sow-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))]
      (should-render-new-sow-view-with-errors
        {:start ["Please enter a start date."]
         :hourly-rate ["Please enter an hourly rate."]
         :projects ["Please select at least one project."]}
        (@sows request)))))
