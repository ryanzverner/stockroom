(ns stockroom.admin.clients.list-clients
  (:require [ring.util.response :as response]
            [stockroom.admin.clients.index-view :refer [render-clients-index-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn are-all-projects-empty [api projects]
  (every? true?
    (map
      (fn [p]
        (let [project-id (:id p)
              sows (:result (api/find-all-sows api {:project-id project-id}))
              engagements (:result (api/find-all-engagements api {:project-id project-id}))]
          (and (empty? sows) (empty? engagements))))
      projects)))

(defn generate-delete-url-if-empty [context api client-id]
  (let [projects (:result (api/find-all-projects-for-client api client-id))]
    (if (or (empty? projects) (are-all-projects-empty api projects))
      (urls/delete-client-url context {:client-id client-id})
      nil)))

(defn build-view-data-for-clients-index-view [{:keys [clients context api]}]
  {:new-client-url (urls/new-client-url context)
   :clients (map
              (fn [c]
                {:id (:id c)
                 :name (:name c)
                 :show-url (urls/show-client-url context {:client-id (:id c)})
                 :edit-url (urls/edit-client-url context {:client-id (:id c)})
                 :delete-url (generate-delete-url-if-empty context api (:id c))})
              clients)})

(defn list-clients [context {:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api clients]
        (-> {:clients clients :context context :api api}
          build-view-data-for-clients-index-view
          render-clients-index-view
          response/response))
      (api/find-all-clients api))))
