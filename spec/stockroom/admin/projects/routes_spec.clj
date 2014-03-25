(ns stockroom.admin.projects.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.projects.new-view :refer [render-new-project-view]]
            [stockroom.admin.projects.edit-view :refer [render-edit-project-view]]
            [stockroom.admin.projects.routes :refer [handler]]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-new-project-view-with-errors [errors & body]
  `(let [f# (stub :render-new-project-view)]
     (with-redefs [render-new-project-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-project-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(defmacro should-render-edit-project-view-with-errors [errors & body]
  `(let [f# (stub :render-edit-project-view)]
     (with-redefs [render-edit-project-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-edit-project-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.projects.routes"

  (with-stubs)

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with projects (handler @ctx))

  (it "renders the new page"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :get (urls/new-project-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))
          response (@projects request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders not found if the client does not exist"
    (let [request (-> (request :get (urls/new-project-url @ctx {:client-id "10"}))
                    (wring/set-user-api @api))
          response (@projects request)]
      (should-render-not-found response)))

  (it "creates a project"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :post
                               (urls/create-project-url @ctx {:client-id client-id})
                               {:params {:name "project name"}})
                    (wring/set-user-api api))
          response (@projects request)
          api (wring/user-api response)
          {projects :result} (api/find-all-projects-for-client api client-id)
          created-project (first projects)]
      (should= 1 (count projects))
      (should= "project name" (:name created-project))
      (should-redirect-after-post-to response (urls/show-client-url @ctx {:client-id client-id}))
      (should= "Successfully created project."
               (-> response :flash :success))))

  (it "re-renders the new view when validation errors"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          request (-> (request :post (urls/create-project-url @ctx {:client-id client-id}))
                    (wring/set-user-api api))]
      (should-render-new-project-view-with-errors
        {:name ["Please enter a name."]}
        (@projects request))))

  (it "renders the edit view"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          {api :api project-id :result} (api/create-project! api {:name "test project"})
          request (-> (request :get
                               (urls/edit-project-url @ctx {:project-id project-id
                                                            :client-id client-id}))
                    (wring/set-user-api api))
          response (@projects request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "updates a project"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          {api :api project-id :result} (api/create-project! api {:name "test project"})
          request (-> (request :put
                               (urls/update-project-url @ctx {:project-id project-id
                                                              :client-id client-id})
                               {:params {:name "new name"}})
                    (wring/set-user-api api))
          response (@projects request)
          api (wring/user-api response)
          {updated-project :result} (api/find-project-by-id api project-id)]
      (should= "new name" (:name updated-project))
      (should-redirect-after-post-to response (urls/show-client-url @ctx {:client-id client-id}))
      (should= "Successfully updated project."
               (-> response :flash :success))))

  (it "re-renders the edit view when there are validation errors"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          {api :api project-id :result} (api/create-project! api {:name "test project"})
          request (-> (request :put
                               (urls/update-project-url @ctx {:project-id project-id
                                                              :client-id client-id})
                               {:params {:name ""}})
                    (wring/set-user-api api))
          response (should-render-edit-project-view-with-errors
                     {:name ["Please enter a name."]}
                     (@projects request))
          api (wring/user-api response)
          {project :result} (api/find-project-by-id api project-id)]
      (should= "test project" (:name project))))

   (it "deletes a project from an client"
    (let [{api :api client-id :result} (api/create-client! @api {:name "test"})
          {api :api project-id :result} (api/create-project! api {:name "test project" :client-id client-id})
          request (-> (request :delete (urls/delete-project-url @ctx {:client-id client-id :project-id project-id})
                               {:params {:project-id project-id}})
                    (wring/set-user-api api))
          response (@projects request)]
      (should-redirect-after-post-to response (urls/show-client-url @ctx {:client-id client-id}))
      (should= "Removed project." (:success (:flash response))))))
