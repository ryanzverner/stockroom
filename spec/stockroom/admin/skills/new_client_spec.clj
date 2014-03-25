(ns stockroom.admin.skills.new-skill-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.skills.new-skill :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.skills.new-skill"

  (with ctx (test-admin-context))

  (it "builds view-data for the new skill view"
    (should= {:create-skill-url (urls/create-skill-url @ctx)
              :errors :errors}
             (build-view-data-for-new-skill-view {:context @ctx
                                                   :errors :errors})))

  )
