(ns stockroom.admin.skills.edit-skill
  (:require [ring.util.response :as response]
            [stockroom.admin.skills.edit-view :refer [render-edit-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-edit-skill-view [{:keys [request errors context skill]}]
  (let [params (:params request)]
    {:skill-name (:name skill)
     :params {:name (or (:name params) (:name skill))}
     :errors errors
     :update-skill-url (urls/update-skill-url context {:skill-id (:id skill)})}))

(defn respond-with-edit-skill-view [{:keys [skill-id errors request context response-status]}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api skill]
        (-> {:context context :request request :errors errors :skill skill}
          build-view-data-for-edit-skill-view
          render-edit-skill-view
          response/response
          (response/status response-status)
          (wring/set-user-api api)))
      (api/find-skill-by-id api skill-id))))

(defn edit-skill [context request]
  (respond-with-edit-skill-view {:skill-id (-> request :params :id)
                                  :request request
                                  :errors {}
                                  :response-status 200
                                  :context context}))
