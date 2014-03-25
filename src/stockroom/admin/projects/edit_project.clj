(ns stockroom.admin.projects.edit-project
  (:require [ring.util.response :as response]
            [stockroom.admin.projects.edit-view :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn set-checked [skill project-skills]
  (let [skill-id (:id skill)
        project-skills-skill-ids (map :id project-skills)
        checked (boolean (some #{skill-id} project-skills-skill-ids))]
    (assoc skill :checked checked)))

(defn build-view-data-for-edit-project-view [{:keys [client-id project skills project-skills context errors params]}]
  {:errors errors
   :params {:name (or (:name params) (:name project))}
   :project-name (:name project)
   :skills (map #(set-checked % project-skills) skills)
   :update-project-url (urls/update-project-url context {:client-id client-id
                                                         :project-id (:id project)})})

(defn respond-with-edit-project-view [{:keys [request response-status] :as options}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api project]
        (when-status
          :success
          (fn [api skills]
            (when-status
              :success
              (fn [api skills-for-project]
                (-> (assoc options :project project :skills skills :project-skills skills-for-project)
                  build-view-data-for-edit-project-view
                  render-edit-project-view
                  response/response
                  (response/status response-status)
                  (wring/set-user-api api)))
            (api/find-all-skills-for-project api (:id project))))
          (api/find-all-skills api)))
      (api/find-project-by-id api (:project-id options)))))

(defn edit-project [context request]
  (respond-with-edit-project-view {:request request
                                   :response-status 200
                                   :project-id (-> request :params :project-id)
                                   :client-id (-> request :params :client-id)
                                   :context context
                                   :errors {}
                                   :params (:params request)}))
