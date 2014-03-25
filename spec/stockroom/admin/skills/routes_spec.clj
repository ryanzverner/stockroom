(ns stockroom.admin.skills.routes-spec
  (:require [speclj.core :refer :all]
            [speclj.stub :refer [first-invocation-of]]
            [stockroom.admin.skills.routes :refer [handler]]
            [stockroom.admin.skills.new-view :refer [render-new-skill-view]]
            [stockroom.admin.skills.edit-view :refer [render-edit-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defmacro should-render-new-skill-view-with-errors [errors & body]
  `(let [f# (stub :render-new-skill-view)]
     (with-redefs [render-new-skill-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-new-skill-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(defmacro should-render-edit-skill-view-with-errors [errors & body]
  `(let [f# (stub :render-edit-skill-view)]
     (with-redefs [render-edit-skill-view f#]
       (let [response# (do ~@body)
             view-data# (first (first-invocation-of :render-edit-skill-view))]
         (should= 422 (:status response#))
         (should= ~errors (:errors view-data#))
         response#))))

(describe "stockroom.admin.skills.routes"

  (with-stubs)
  (with api (test-stockroom-api))
  (with ctx (test-admin-context))
  (with skills (handler @ctx))

  (it "renders the new skills page"
    (let [request (request :get (urls/new-skill-url @ctx))
          response (@skills request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders the show skill page"
    (let [{api :api skill-id :result} (api/create-skill! @api {:name "test"})
          request (-> (request :get (urls/show-skill-url @ctx {:skill-id skill-id}))
                    (wring/set-user-api api))
          response (@skills request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "renders not found when the skill does not exist"
    (let [request (-> (request :get (urls/show-skill-url @ctx {:skill-id "adf"}))
                    (wring/set-user-api @api))
          response (@skills request)]
      (should-render-not-found response)))

  (it "renders the edit skill page"
    (let [{api :api skill-id :result} (api/create-skill! @api {:name "test"})
          request (-> (request :get (urls/edit-skill-url @ctx {:skill-id skill-id}))
                    (wring/set-user-api api))
          response (@skills request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "responds with not found when the skill does not exist"
    (let [request (-> (request :get (urls/edit-skill-url @ctx {:skill-id "10"})
                               {:params {:name "test1"}})
                    (wring/set-user-api @api))
          response (@skills request)]
      (should-render-not-found response)))

  (it "renders the skills index"
    (let [request (-> (request :get (urls/list-skills-url @ctx))
                    (wring/set-user-api @api))
          response (@skills request)]
      (should= 200 (:status response))
      (should-not-be-nil (:body response))))

  (it "creates a skill"
    (let [request (-> (request :post (urls/create-skill-url @ctx)
                               {:params {:name "Skill Name"}})
                    (wring/set-user-api @api))
          response (@skills request)
          api (wring/user-api response)
          {users :result} (api/find-all-skills api)
          created-user (first users)]
      (should= 1 (count users))
      (should= "Skill Name" (:name created-user))
      (should-redirect-after-post-to response (urls/list-skills-url @ctx))
      (should= "Successfully created skill."
               (-> response :flash :success))))

  (it "re-renders the new skill page when validation errors"
    (let [request (-> (request :post (urls/create-skill-url @ctx))
                    (wring/set-user-api @api))
          response (should-render-new-skill-view-with-errors
                     {:name ["Please enter a name."]}
                     (@skills request))]
      (should= 0 (count (:result (api/find-all-skills (wring/user-api response)))))))

  (it "updates a skill"
    (let [{api :api skill-id :result} (api/create-skill! @api {:name "test"})
          request (-> (request :put (urls/update-skill-url @ctx {:skill-id skill-id})
                               {:params {:name "new Skill Name"}})
                    (wring/set-user-api api))
          response (@skills request)
          api (wring/user-api response)
          {updated-user :result} (api/find-skill-by-id api skill-id)]
      (should= "new Skill Name" (:name updated-user))
      (should-redirect-after-post-to response (urls/list-skills-url @ctx))
      (should= "Successfully updated skill."
               (-> response :flash :success))))

  (it "rerenders the edit form when validation errors"
    (let [{api :api skill-id :result} (api/create-skill! @api {:name "test"})
          request (-> (request :put (urls/update-skill-url @ctx {:skill-id skill-id})
                               {:params {:name ""}})
                    (wring/set-user-api api))
          response (should-render-edit-skill-view-with-errors
                     {:name ["Please enter a name."]}
                     (@skills request))
          api (wring/user-api response)
          {updated-user :result} (api/find-skill-by-id api skill-id)]
      (should= "test" (:name updated-user))))

  )
