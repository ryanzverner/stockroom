(ns stockroom.admin.locations.new-location-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.locations.new-location :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.location.new-location"

  (with ctx (test-admin-context))

  (it "builds view-data for the new location view"
    (should= {:create-location-url (urls/create-location-url @ctx)
              :errors :errors}
             (build-view-data-for-location-new {:context @ctx
                                                :errors :errors}))))