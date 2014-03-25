(ns stockroom.admin.locations.list-locations-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.locations.list-locations :refer :all]))

(describe "stockroom.admin.locations.list-locations"

  (with ctx (test-admin-context))

  (it "builds view data for locations index view"
    (let [all-locations [{:id 12 :name "Chicago"} {:id 13 :name "London"}]
          request {}]
      (should= {:locations [{:url (urls/show-location-url @ctx {:location-id 12})
                             :name "Chicago"}
                            {:url (urls/show-location-url @ctx {:location-id 13})
                             :name "London"}]
                :new-location-url (urls/new-location-url @ctx)}
               (build-view-data-for-locations-index-view {:context @ctx
                                                          :all-locations all-locations})))))