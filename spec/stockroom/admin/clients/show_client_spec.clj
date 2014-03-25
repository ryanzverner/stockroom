(ns stockroom.admin.clients.show-client-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.clients.show-client :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-from-now-at-midnight]]))

(def two-days-from-now (days-from-now-at-midnight 2))
(def five-days-from-now (days-from-now-at-midnight 5))

(describe "stockroom.admin.clients.show-client"

  (with ctx (test-admin-context))

  (it "builds view data for show client view"
    (let [client {:id 10 :name "test client"}
          projects [{:id 50
                     :name "test project1"
                     :sows []
                     :delete-url "/clients/10/projects/50"}
                    {:id 51
                     :name "test project2"
                     :sows [{:id 5
                             :start (date-for-input two-days-from-now)
                             :hourly-rate 500}
                            {:id 6
                             :start (date-for-input five-days-from-now)
                             :hourly-rate 250}]}]]
      (should= {:client-name "test client"
                :new-project-url "/clients/10/projects/new"
                :new-sow-url "/clients/10/sows/new"
                :projects [{:id 50
                            :name "test project1"
                            :sows []
                            :delete-url "/clients/10/projects/50"}
                           {:id 51
                            :name "test project2"
                            :sows [{:id 5, :start (date-for-input two-days-from-now), :hourly-rate 500}
                                   {:id 6, :start (date-for-input five-days-from-now), :hourly-rate 250}]}]}
               (build-view-data-for-show-client-view {:context @ctx
                                                      :client client
                                                      :projects projects})))))
