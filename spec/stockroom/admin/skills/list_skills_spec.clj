(ns stockroom.admin.skills.list-skills-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.skills.list-skills :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.skills.list-skills"

  (with ctx (test-admin-context))

  (it "builds view data for skills index view"
    (let [skills [{:id 10 :name "test1"}
                   {:id 20 :name "test2"}]]
      (should= {:new-skill-url (urls/new-skill-url @ctx)
                :skills [{:name "test1"
                           :show-url (urls/show-skill-url @ctx {:skill-id 10})
                           :edit-url (urls/edit-skill-url @ctx {:skill-id 10})}
                          {:name "test2"
                           :show-url (urls/show-skill-url @ctx {:skill-id 20})
                           :edit-url (urls/edit-skill-url @ctx {:skill-id 20})}]}
               (build-view-data-for-skills-index-view {:context @ctx
                                                        :skills skills}))))

  )
