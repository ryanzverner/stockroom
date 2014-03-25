(ns stockroom.admin.sows.show-sow
  (:require [ring.util.response :as response]
            [stockroom.admin.sows.show-view :refer [render-show-sow-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-show-sow-view [{:keys [context client projects sow]}]
  {:client-name (:name client)
   :projects projects
   :sow sow
   :edit-sow-url (urls/edit-sow-url context {:client-id (:id client) :sow-id (:id sow)})
   :delete-sow-url (urls/delete-sow-url context {:client-id (:id client) :sow-id (:id sow)})})

(defn show-sow [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:client-id params)
        sow-id (:id params)]
    (when-status
      :success
      (fn [api client]
        (when-status
          :success
          (fn [api projects]
            (when-status
              :success
              (fn [api sow]
                (-> {:context context
                     :client client
                     :projects projects
                     :sow sow}
                  build-view-data-for-show-sow-view
                  render-show-sow-view
                  response/response
                  (wring/set-user-api api)))
              (api/find-sow-by-id api sow-id)))
          (api/find-all-projects api {:sow-id sow-id})))
      (api/find-client-by-id api client-id))))
