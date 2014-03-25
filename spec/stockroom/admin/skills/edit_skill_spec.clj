(ns stockroom.admin.skills.edit-skill-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.skills.edit-skill :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.skills.edit-skill"

  (with ctx (test-admin-context))

  (it "builds view data for edit skill view"
    (let [skill {:id 10 :name "test-skill"}
          request {:params {}}
          errors  {:name ["some errors"]}]
      (should= {:skill-name "test-skill"
                :errors errors
                :params {:name "test-skill"}
                :update-skill-url (urls/update-skill-url @ctx {:skill-id 10})}
               (build-view-data-for-edit-skill-view {:request request
                                                      :context @ctx
                                                      :errors errors
                                                      :skill skill}))))

  (it "uses the name from the params if present"
    (let [skill {:id 10 :name "test-skill"}
          request {:params {:name "abc"}}
          errors  {:name ["some errors"]}
          view-data (build-view-data-for-edit-skill-view {:request request
                                                           :context @ctx
                                                           :errors errors
                                                           :skill skill})]
      (should= "abc" (-> view-data :params :name))))

  )
