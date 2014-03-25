(ns stockroom.admin.clients.edit-client-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.clients.edit-client :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.clients.edit-client"

  (with ctx (test-admin-context))

  (it "builds view data for edit client view"
    (let [client {:id 10 :name "test-client"}
          request {:params {}}
          errors  {:name ["some errors"]}]
      (should= {:client-name "test-client"
                :errors errors
                :params {:name "test-client"}
                :update-client-url (urls/update-client-url @ctx {:client-id 10})}
               (build-view-data-for-edit-client-view {:request request
                                                      :context @ctx
                                                      :errors errors
                                                      :client client}))))

  (it "uses the name from the params if present"
    (let [client {:id 10 :name "test-client"}
          request {:params {:name "abc"}}
          errors  {:name ["some errors"]}
          view-data (build-view-data-for-edit-client-view {:request request
                                                           :context @ctx
                                                           :errors errors
                                                           :client client})]
      (should= "abc" (-> view-data :params :name))))

  )
