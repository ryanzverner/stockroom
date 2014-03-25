(ns stockroom.admin.sows.new-sow-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.sows.new-sow :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.sows.new-sow"

  (with ctx (test-admin-context))

  (it "builds view data for new SOW view"
    (let [params {}
          errors {:start ["one"]}
          projects [{:id 12 :client-id 10 :name "Project 1"}
                    {:id 13 :client-id 10 :name "Project 2"}]]
      (should= {:create-sow-url (urls/create-sow-url @ctx {:client-id nil})
                :params {:currency-code "USD"}
                :projects projects
                :errors errors
                :currency-code-options [["USD" "USD"]
                                        ["GBP" "GBP"]
                                        ["EUR" "EUR"]]}
               (build-view-data-for-new-sow-view {:context @ctx
                                                  :params params
                                                  :projects projects
                                                  :errors errors}))))

  (it "sets USD as default currency"
    (let [params {}
          errors {:start ["one"]}
          projects [{:id 12 :client-id 10 :name "Project 1"}
                    {:id 13 :client-id 10 :name "Project 2"}]
          view-data (build-view-data-for-new-sow-view {:context @ctx
                                                  :params params
                                                  :projects projects
                                                  :errors errors})]
      (should= "USD" (:currency-code (:params view-data))))))