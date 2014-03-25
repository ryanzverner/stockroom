(ns stockroom.admin.sows.edit-sow-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.sows.edit-sow :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]))

(def two-days-ago (days-ago-at-midnight 2))
(def one-day-from-now (days-from-now-at-midnight 1))
(def two-days-from-now (days-from-now-at-midnight 2))
(def five-days-from-now (days-from-now-at-midnight 5))

(describe "stockroom.admin.sows.edit-sow"

  (with ctx (test-admin-context))

  (it "build view data for edit sow view"
    (let [sow {:id 10
               :start (date-for-input one-day-from-now)
               :end (date-for-input two-days-from-now)
               :hourly-rate 225
               :currency-code "USD"
               :signed-date (date-for-input two-days-ago)
               :url "www.example1.com"}
          client-id 20
          projects [{:id 30 :client-id 20 :name "Project 1"}
                    {:id 31 :client-id 20 :name "Project 2"}]
          params {:hourly-rate 175 :currency-code "GBP" :end five-days-from-now}
          view-data (build-view-data-for-edit-sow-view {:context @ctx
                                                        :errors :errors
                                                        :sow sow
                                                        :client-id client-id
                                                        :params params
                                                        :projects projects}) ]
      (should= params (:params view-data))
      (should= (urls/update-sow-url @ctx {:client-id 20 :sow-id 10}) (:update-sow-url view-data))
      (should= :errors (:errors view-data))
      (should= projects (map #(select-keys % [:id :client-id :name]) (:projects view-data)))))

  (it "pre-checks project checkboxes when project-sow exists"
    (let [sow {:id 10
               :start (date-for-input one-day-from-now)
               :end (date-for-input two-days-from-now)
               :hourly-rate 225
               :currency-code "USD"
               :signed-date (date-for-input two-days-ago)
               :url "www.example1.com"}
          client-id 20
          sow-project {:id 30 :client-id 20 :name "Project 1"}
          non-sow-project {:id 31 :client-id 20 :name "Project 2"}
          params {}
          view-data (build-view-data-for-edit-sow-view {:context @ctx
                                                        :errors :errors
                                                        :sow sow
                                                        :client-id client-id
                                                        :params params
                                                        :projects [sow-project non-sow-project]
                                                        :sow-projects [sow-project]})]
      (should= {} (:params view-data))
      (should= (urls/update-sow-url @ctx {:client-id 20 :sow-id 10}) (:update-sow-url view-data))
      (should= :errors (:errors view-data))
      (should= [(assoc sow-project :checked true) (assoc non-sow-project :checked false)] (:projects view-data))))

  (it "uses sow data when params not present"
    (let [sow {:id 10
               :start (date-for-input one-day-from-now)
               :end (date-for-input two-days-from-now)
               :hourly-rate 225
               :currency-code "USD"
               :signed-date (date-for-input two-days-ago)
               :url "www.example1.com"}
          client-id 20
          projects [{:id 30 :client-id 20 :name "Project 1"}
                    {:id 31 :client-id 20 :name "Project 2"}]
          params {}
          view-data (build-view-data-for-edit-sow-view {:context @ctx
                                                        :errors :errors
                                                        :sow sow
                                                        :client-id client-id
                                                        :params params
                                                        :projects projects})]
      (should= {} (:params view-data))
      (should= :errors (:errors view-data))
      (should= (urls/update-sow-url @ctx {:client-id 20 :sow-id 10}) (:update-sow-url view-data))
      (should= projects (map #(select-keys % [:id :client-id :name]) (:projects view-data))))))
