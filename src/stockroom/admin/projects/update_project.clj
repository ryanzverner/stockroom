(ns stockroom.admin.projects.update-project
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.projects.edit-project :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-edit-project-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn update-project [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        project-id (:project-id params)
        client-id (:client-id params)
        skills (:skills params)
        errors (validate-edit-project-request params)]
    (if (seq errors)
      (respond-with-edit-project-view {:context context
                                       :project-id project-id
                                       :client-id client-id
                                       :response-status 422
                                       :errors errors
                                       :request request})
    (when-status
      :success
      (fn [api _]
        (api/delete-project-skills-for-project! api project-id)
        (doseq [skill-id (flatten (vector skills))]
          (api/create-project-skill! api {:skill-id skill-id :project-id project-id}))
        (-> (urls/show-client-url context {:client-id client-id})
          response/redirect-after-post
          (wring/set-user-api api)
          (assoc-in [:flash :success] "Successfully updated project.")))
      (api/update-project! api project-id {:name (:name params)})))))
