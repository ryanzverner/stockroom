(ns stockroom.admin.projects.new-project
  (:require [ring.util.response :as response]
            [stockroom.admin.projects.new-view :refer [render-new-project-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-new-project-view [{:keys [client context errors params]}]
  {:create-project-url (urls/create-project-url context {:client-id (:id client)})
   :errors errors
   :params params})

(defn respond-with-new-project-view [{:keys [request context client-id params errors response-status]}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api client]
        (-> {:context context :errors errors :params params :client client}
          build-view-data-for-new-project-view
          render-new-project-view
          response/response
          (response/status response-status)))
      (api/find-client-by-id api client-id))))

(defn new-project [context request]
  (respond-with-new-project-view {:context context
                                  :request request
                                  :response-status 200
                                  :client-id (-> request :params :client-id)
                                  :params (:params request)
                                  :errors {}}))
