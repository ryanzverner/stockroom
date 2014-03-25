(ns stockroom.admin.sows.show-sow-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.sows.show-sow :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]))

(def one-day-ago (days-ago-at-midnight 1))
(def two-days-from-now (days-from-now-at-midnight 2))
(def five-days-from-now (days-from-now-at-midnight 5))

(describe "stockroom.admin.sows.show-sow"

  (with ctx (test-admin-context))

  (it "builds view data for show sow view"
    (let [client {:id 10 :name "test client"}
          sow {:id 11
               :start (date-for-input two-days-from-now)
               :end (date-for-input five-days-from-now)
               :hourly-rate 200
               :currency-code "USD"
               :signed-date (date-for-input one-day-ago)
               :url "www.example.com"}
          projects [{:id 30 :name "Project 1"}
                    {:id 31 :name "Project 2"}]]
      (should= {:client-name "test client"
                :projects [{:name "Project 1" :id 30}
                           {:name "Project 2" :id 31}]
                :sow {:id 11
                      :start (date-for-input two-days-from-now)
                      :end (date-for-input five-days-from-now)
                      :hourly-rate 200
                      :currency-code "USD"
                      :signed-date (date-for-input one-day-ago)
                      :url "www.example.com"}
                :edit-sow-url "/clients/10/sows/11/edit"
                :delete-sow-url "/clients/10/sows/11"}
               (build-view-data-for-show-sow-view {:context @ctx
                                                   :client client
                                                   :projects projects
                                                   :sow sow})))))