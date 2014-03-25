(ns stockroom.admin.clients.new-client-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.clients.new-client :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.clients.new-client"

  (with ctx (test-admin-context))

  (it "builds view-data for the new client view"
    (should= {:create-client-url (urls/create-client-url @ctx)
              :errors :errors}
             (build-view-data-for-new-client-view {:context @ctx
                                                   :errors :errors})))

  )
