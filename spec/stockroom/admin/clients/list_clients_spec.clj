(ns stockroom.admin.clients.list-clients-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.clients.list-clients :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context test-stockroom-api]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.clients.list-clients"

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))

  (it "builds view data for clients index view"
    (let [clients [{:id 10 :name "test1"}
                   {:id 20 :name "test2"}]]
      (should= {:new-client-url (urls/new-client-url @ctx)
                :clients [{:id 10
                           :name "test1"
                           :show-url (urls/show-client-url @ctx {:client-id 10})
                           :edit-url (urls/edit-client-url @ctx {:client-id 10})
                           :delete-url (urls/delete-client-url @ctx {:client-id 10})}
                          {:id 20
                           :name "test2"
                           :show-url (urls/show-client-url @ctx {:client-id 20})
                           :edit-url (urls/edit-client-url @ctx {:client-id 20})
                           :delete-url (urls/delete-client-url @ctx {:client-id 20})}]}
               (build-view-data-for-clients-index-view {:context @ctx
                                                        :clients clients
                                                        :api @api}))))

  )
