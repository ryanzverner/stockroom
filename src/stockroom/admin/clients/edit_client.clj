(ns stockroom.admin.clients.edit-client
  (:require [ring.util.response :as response]
            [stockroom.admin.clients.edit-view :refer [render-edit-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-edit-client-view [{:keys [request errors context client]}]
  (let [params (:params request)]
    {:client-name (:name client)
     :params {:name (or (:name params) (:name client))}
     :errors errors
     :update-client-url (urls/update-client-url context {:client-id (:id client)})}))

(defn respond-with-edit-client-view [{:keys [client-id errors request context response-status]}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api client]
        (-> {:context context :request request :errors errors :client client}
          build-view-data-for-edit-client-view
          render-edit-client-view
          response/response
          (response/status response-status)
          (wring/set-user-api api)))
      (api/find-client-by-id api client-id))))

(defn edit-client [context request]
  (respond-with-edit-client-view {:client-id (-> request :params :id)
                                  :request request
                                  :errors {}
                                  :response-status 200
                                  :context context}))
