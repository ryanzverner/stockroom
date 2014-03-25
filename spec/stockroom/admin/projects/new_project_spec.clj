(ns stockroom.admin.projects.new-project-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.projects.new-project :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.projects.new-project"

  (with ctx (test-admin-context))

  (it "builds view data for the new project view"
    (let [client {:id 50}]
      (should= {:create-project-url (urls/create-project-url @ctx {:client-id 50})
                :errors :errors
                :params {:name "test name"}}
               (build-view-data-for-new-project-view {:context @ctx
                                                      :client client
                                                      :errors :errors
                                                      :params {:name "test name"}}))))

  )
