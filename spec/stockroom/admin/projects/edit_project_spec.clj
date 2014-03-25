(ns stockroom.admin.projects.edit-project-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.projects.edit-project :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.projects.edit-project"

  (with ctx (test-admin-context))

  (it "build view data for edit project view"
    (let [project {:id 10 :name "test"}
          client-id 20
          skills [{:id 1 :name "Ruby"} {:id 2 :name "Python"}]
          project-skills [{:id 2 :name "Python"}]
          params {:name "test1"}]
      (should= {:project-name "test"
                :skills [{:id 1 :name "Ruby" :checked false} {:id 2 :name "Python" :checked true}]
                :params {:name "test1"}
                :update-project-url (urls/update-project-url @ctx {:client-id 20
                                                                   :project-id 10})
                :errors :errors}
               (build-view-data-for-edit-project-view {:context @ctx
                                                       :errors :errors
                                                       :project project
                                                       :client-id client-id
                                                       :skills skills
                                                       :project-skills project-skills
                                                       :params params}))))

  (it "uses project data when params not present"
    (let [project {:id 10 :name "test"}
          client-id 20
          params {}]
      (should= {:project-name "test"
                :skills ()
                :params {:name "test"}
                :update-project-url (urls/update-project-url @ctx {:client-id 20
                                                                   :project-id 10})
                :errors :errors}
               (build-view-data-for-edit-project-view {:context @ctx
                                                       :errors :errors
                                                       :project project
                                                       :client-id client-id
                                                       :params params}))))
  )
