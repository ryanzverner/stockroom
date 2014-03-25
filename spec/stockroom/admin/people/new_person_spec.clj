(ns stockroom.admin.people.new-person-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.people.new-person :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.people.new-person"

  (with ctx (test-admin-context))

  (it "builds view data for new view"
    (let [params {:a 1}
          errors {:first-name ["one"]}]
      (should= {:create-person-url (urls/create-person-url @ctx)
                :params params
                :errors errors}
               (build-view-data-for-new-view {:context @ctx
                                              :params params
                                              :errors errors}))))

  )

