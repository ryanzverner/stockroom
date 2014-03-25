(ns stockroom.admin.sows.edit-sow
  (:require [ring.util.response :as response]
            [stockroom.admin.sows.edit-view :refer :all]
            [stockroom.admin.sows.form :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-edit-sow-view [{:keys [context client-id sow] :as options}]
  (merge {:update-sow-url (urls/update-sow-url context {:client-id client-id :sow-id (:id sow)})}
         (build-view-data-for-sow-form options)))

(defn respond-with-edit-sow-view [{:keys [context errors request response-status] :as options}]
  (let [api (wring/user-api request)
        params (:params request)
        sow-id (:sow-id params)
        client-id (:client-id params)]
    (when-status
      :success
      (fn [api sow]
        (respond-with-sow-form
          (fn [form-view-data form-body]
            (-> {:context context
                 :client-id client-id
                 :sow sow}
              build-view-data-for-edit-sow-view
              (render-edit-sow-view form-body)))
          {:context context
           :request request
           :params (sow->form-params sow params)
           :errors errors
           :sow sow-id
           :response-status response-status}))
      (api/find-sow-by-id api sow-id))))

(defn edit-sow [context request]
  (respond-with-edit-sow-view {:request request
                               :response-status 200
                               :context context
                               :errors {}}))
