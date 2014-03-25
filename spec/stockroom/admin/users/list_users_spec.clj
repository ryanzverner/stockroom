(ns stockroom.admin.users.list-users-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.list-users :refer :all]))

(describe "stockroom.admin.users.list-users"

  (with ctx (test-admin-context))

  (it "builds view data for users index view"
    (let [all-users [{:id 50 :name "one"} {:id 60 :name "two"}]
          request {}]
      (should= {:users [{:url (urls/show-user-url @ctx {:user-id 50})
                         :name "one"}
                        {:url (urls/show-user-url @ctx {:user-id 60})
                         :name "two"}]
                :new-user-url (urls/new-user-url @ctx)}
               (build-view-data-for-users-index-view {:context @ctx
                                                      :all-users all-users}))))

  )
