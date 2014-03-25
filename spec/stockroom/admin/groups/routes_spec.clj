(ns stockroom.admin.groups.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of invocations-of]]
            [stockroom.admin.groups.routes :refer [handler build-view-data-for-group-show
                                         build-view-data-for-group-index]]
            [stockroom.admin.groups.new-view :refer [render-new-group-view]]
            [stockroom.admin.groups.show-view :refer [render-show-group-view]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.api :as api]
            [stockroom.v1.permissions :as permissions]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-show-group-with-errors [errors & body]
  `(let [f# (stub :render-show-group-view)]
     (with-redefs [render-show-group-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-show-group-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))))))

(defmacro should-render-new-group-view-with-errors [errors & body]
  `(let [f# (stub :render-new-group-view)]
     (with-redefs [render-new-group-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-group-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.groups.routes"
  (with-stubs)
  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with groups (handler @ctx))

  (it "renders the new group page"
    (let [request (request :get (urls/new-group-url @ctx))
          response (@groups request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a group"
    (let [request (-> (request :post (urls/create-group-url @ctx) {:params {:name "Admin group"}})
                    (wring/set-user-api @api))
          response (@groups request)
          api (wring/user-api response)
          {groups :result} (api/find-all-permission-groups api)
          created-group (first groups)]
      (should= 1 (count groups))
      (should= "Admin group" (:name created-group))
      (should-redirect-after-post-to response (urls/list-groups-url @ctx))
      (should= "Successfully created group." (:success (:flash response)))))

  (it "re-renders the new page when errors"
    (let [request (-> (request :post (urls/create-group-url @ctx) {:params {:name ""}})
                    (wring/set-user-api @api))
          response (should-render-new-group-view-with-errors
                                 {:name ["Please enter a name for the group."]}
                                 (@groups request))
          api (wring/user-api response)
          {groups :result} (api/find-all-permission-groups api)]
      (should= 0 (count groups))))

  (it "re-renders the new page when name is not unique"
    (let [{api :api} (api/create-permissions-group! @api {:name "test"})
          request (-> (request :post (urls/create-group-url @ctx) {:params {:name "test"}})
                    (wring/set-user-api api))
          response (should-render-new-group-view-with-errors
                     {:name ["This group name is already taken. Please choose another name."]}
                     (@groups request))
          api (wring/user-api response)
          {groups :result} (api/find-all-permission-groups api)]
      (should= 1 (count groups))))

  (it "adds a permission to a group"
    (let [permission (first permissions/all-permissions)
          {api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          add-permission-url (urls/add-permission-url @ctx {:group-id group-id})
          request (-> (request :post add-permission-url {:params {:permission permission}})
                    (wring/set-user-api api))
          response (@groups request)
          api (wring/user-api response)
          {permissions :result} (api/find-permissions-for-group api group-id)]
      (should= [permission] permissions)
      (should-redirect-after-post-to response (urls/show-group-url @ctx {:group-id group-id}))
      (should= (format "Added \"%s\" permission." permission) (:success (:flash response)))))

  (it "does not add a permission to a group if it is not present"
    (let [{api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          add-permission-url (urls/add-permission-url @ctx {:group-id group-id})
          request (-> (request :post add-permission-url {:params {:permission ""}})
                    (wring/set-user-api api))]
      (should-render-show-group-with-errors
        {:permission ["Please choose a permission."]}
        (@groups request))))

  (it "does not add a permission to a group if it is not a valid permission"
    (let [permission (first permissions/all-permissions)
          {api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          add-permission-url (urls/add-permission-url @ctx {:group-id group-id})
          request (-> (request :post add-permission-url {:params {:permission "unknown permission"}})
                    (wring/set-user-api api))]
      (should-render-show-group-with-errors
        {:permission ["Please choose a valid permission."]}
        (@groups request))))

  (it "renders not found when the group does not exist"
    (let [add-permission-url (urls/add-permission-url @ctx {:group-id "10"})
          request (-> (request :post add-permission-url {:params {:permission "test"}})
                    (wring/set-user-api @api))
          response (@groups request)]
      (should-render-not-found response)))

  (it "renders the show group page"
    (let [{api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          show-group-url (urls/show-group-url @ctx {:group-id group-id})
          request (-> (request :get show-group-url)
                    (wring/set-user-api api))
          response (@groups request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders not found when the group does not exist"
    (let [show-group-url (urls/show-group-url @ctx {:group-id "10"})
          request (-> (request :get show-group-url)
                    (wring/set-user-api @api))
          response (@groups request)]
      (should-render-not-found response)))

  (it "builds view data for the show page when there existing permissions and permission that can be added"
    (let [permissions-in-group ["users/edit" "users/add"]
          all-permissions ["users/edit" "users/add" "users/create" "users/test"]
          users-in-group [{:id 20 :name "twenty"} {:id 30 :name "thirty"}]
          all-users [{:id 10 :name "ten name"} {:id 20 :name "twenty"}
                     {:id 30 :name "thirty"} {:id 50 :name "fifty"}]
          group {:id 50 :name "my group"}
          request {}]
      (should= {:group-name "my group"
                :errors :errors

                :can-add-permission? true
                :show-group-permissions? true
                :add-permission-options [["Select a permission" ""]
                                         ["users/create" "users/create"]
                                         ["users/test" "users/test"]]
                :add-permission-to-group-url (urls/add-permission-url @ctx {:group-id 50})
                :remove-permission-url (urls/remove-permission-url @ctx {:group-id 50})
                :permissions-in-group ["users/edit" "users/add"]

                :show-group-users? true
                :can-add-users? true
                :add-user-options [["Select a user" ""]
                                   ["ten name" 10]
                                   ["fifty" 50]]
                :add-user-to-group-url (urls/add-user-to-group-url @ctx {:group-id 50})
                :remove-user-from-group-url (urls/remove-user-from-group-url @ctx {:group-id 50})
                :users-in-group [{:user-url (urls/show-user-url @ctx {:user-id 20})
                                  :display "twenty"
                                  :remove-value 20}
                                 {:user-url (urls/show-user-url @ctx {:user-id 30})
                                  :display "thirty"
                                  :remove-value 30}]}
               (build-view-data-for-group-show {:group group
                                                :errors :errors
                                                :context @ctx
                                                :permissions-in-group permissions-in-group
                                                :all-permissions all-permissions
                                                :users-in-group users-in-group
                                                :all-users all-users}))))

  (it "sets can-add-permission? to false when there are no new permissions to add"
    (let [all-permissions ["users/edit" "users/add" "users/create" "users/test"]
          permissions-in-group all-permissions
          group {:id 50 :name "my group"}
          view-data (build-view-data-for-group-show {:group group
                                                     :context @ctx
                                                     :permissions-in-group permissions-in-group
                                                     :all-users []
                                                     :users-in-group []
                                                     :all-permissions all-permissions})]
      (should= false (:can-add-permission? view-data))))

  (it "sets show-group-permissions? to false when the group has no permissions"
    (let [all-permissions ["users/edit" "users/add" "users/create" "users/test"]
          permissions-in-group []
          group {:id 50 :name "my group"}
          view-data (build-view-data-for-group-show {:group group
                                                     :context @ctx
                                                     :all-users []
                                                     :users-in-group []
                                                     :permissions-in-group permissions-in-group
                                                     :all-permissions all-permissions})]
      (should= false (:show-group-permissions? view-data))))

  (it "removes a permission from a group"
    (let [permission-to-remove (first permissions/all-permissions)
          {api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          {api :api} (api/add-permission-to-group! api {:group-id group-id :permission permission-to-remove})
          request (-> (request :delete (urls/remove-permission-url @ctx {:group-id group-id})
                               {:params {:permission permission-to-remove}})
                    (wring/set-user-api api))
          response (@groups request)]
      (should-redirect-after-post-to response (urls/show-group-url @ctx {:group-id group-id}))
      (should= (format "Removed permission \"%s\"." permission-to-remove)
               (:success (:flash response)))))

  (it "adds a user to group"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "abc"})
          {api :api group-id :result} (api/create-permissions-group! api {:name "test"})
          add-url (urls/add-user-to-group-url @ctx {:group-id group-id})
          request (-> (request :post add-url {:params {:user-id user-id}})
                    (wring/set-user-api api))
          response (@groups request)
          api (wring/user-api response)
          {users :result} (api/find-all-users-in-group api group-id)]
      (should= [user-id] (map :id users))
      (should-redirect-after-post-to response (urls/show-group-url @ctx {:group-id group-id}))
      (should= "Added user." (:success (:flash response)))))

  (it "does not add a user to a group if it is not present"
    (let [{api :api group-id :result} (api/create-permissions-group! @api {:name "test"})
          add-url (urls/add-user-to-group-url @ctx {:group-id group-id})
          request (-> (request :post add-url {:params {:user-id ""}})
                    (wring/set-user-api api))]
      (should-render-show-group-with-errors
        {:user ["Please select a user."]}
        (@groups request))))

  (it "removes a user from a group"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "abc"})
          {api :api group-id :result} (api/create-permissions-group! api {:name "test"})
          {api :api} (api/add-user-to-group! api {:group-id group-id :user-id user-id})
          request (-> (request :delete (urls/remove-user-from-group-url @ctx {:group-id group-id})
                               {:params {:user-id user-id}})
                    (wring/set-user-api api))
          response (@groups request)]
      (should-redirect-after-post-to response (urls/show-group-url @ctx {:group-id group-id}))
      (should= "Removed user." (:success (:flash response)))))

  (it "redirects the the return-url param after removing a user from a group"
    (let [{api :api user-id :result} (api/create-user-with-authentication! @api {:provider :google :uid "abc"})
          {api :api group-id :result} (api/create-permissions-group! api {:name "test"})
          {api :api} (api/add-user-to-group! api {:group-id group-id :user-id user-id})
          return-url "http://google.com"
          request (-> (request :delete (urls/remove-user-from-group-url @ctx {:group-id group-id})
                               {:params {:user-id user-id
                                         :return-url return-url}})
                    (wring/set-user-api api))
          response (@groups request)]
      (should-redirect-after-post-to response return-url)))

  (it "renders the groups page"
    (let [request (-> (request :get (urls/list-groups-url @ctx))
                    (wring/set-user-api @api))
          response (@groups request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "builds view data for group index"
    (let [all-groups [{:id 10 :name "group 10"}
                      {:id 20 :name "group 20"}]]
      (should= {:groups [{:url (urls/show-group-url @ctx {:group-id 10})
                          :name "group 10"}
                         {:url (urls/show-group-url @ctx {:group-id 20})
                          :name "group 20"}]
                :new-group-url (urls/new-group-url @ctx)}
               (build-view-data-for-group-index {:context @ctx
                                                 :all-groups all-groups}))))

  )
