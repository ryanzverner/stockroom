(ns stockroom.admin.clients.update-client
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.clients.edit-client :refer [respond-with-edit-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-update-client-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn update-client [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-update-client-request params)
        client-id (-> request :params :id)]
    (if (seq errors)
      (respond-with-edit-client-view {:context context
                                      :request request
                                      :errors errors
                                      :response-status 422
                                      :client-id client-id})
      (when-status
        :success
        (fn [api _]
          (-> (response/redirect-after-post (urls/list-clients-url context))
            (assoc-in [:flash :success] "Successfully updated client.")
            (wring/set-user-api api)))
        (api/update-client! api client-id {:name (:name params)})))))
