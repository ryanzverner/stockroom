(ns stockroom.admin.users.show-user-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.show-user :refer :all]))

(describe "stockroom.admin.users.show-user"

  (with ctx (test-admin-context))

  (it "builds view data for show user view"
    (let [user {:id 50 :name "my name"}
          groups [{:id 50 :name "group 50"} {:id 60 :name "group 60"}]
          request {}
          return-url (urls/show-user-url @ctx {:user-id 50})]
      (should= {:name "my name"
                :groups [{:name "group 50"
                          :remove-url (urls/remove-user-from-group-url @ctx {:group-id 50})
                          :remove-params [{:name "user-id" :value 50}
                                          {:name "return-url" :value return-url}]}
                         {:name "group 60"
                          :remove-url (urls/remove-user-from-group-url @ctx {:group-id 60})
                          :remove-params [{:name "user-id" :value 50}
                                          {:name "return-url" :value return-url}]}]}
               (build-view-data-for-user-show-view {:context @ctx
                                                    :user user
                                                    :groups groups}))))

  )
