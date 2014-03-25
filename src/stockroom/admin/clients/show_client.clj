(ns stockroom.admin.clients.show-client
  (:require [ring.util.response :as response]
            [stockroom.admin.clients.show-view :refer [render-show-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.admin.util.view-helper :refer [month-day-year]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-show-client-view [{:keys [context client projects]}]
  {:client-name (:name client)
   :new-project-url (urls/new-project-url context {:client-id (:id client)})
   :new-sow-url (urls/new-sow-url context {:client-id (:id client)})
   :projects projects})

(defn format-sow-data [api context client-id project-id]
  (let [sows (:result (api/find-all-sows api {:project-id project-id}))]
    (map
      (fn [s]
        {:id (:id s)
         :start (month-day-year (:start s))
         :end (or (month-day-year (:end s)) "?")
         :show-sow-url (urls/show-sow-url context {:client-id client-id :project-id project-id :sow-id (:id s)})})
      sows)))

(defn generate-delete-url-if-empty [context project-id client-id sows engagements]
  (if (and (empty? sows) (empty? engagements))
    (urls/delete-project-url context {:project-id project-id :client-id client-id})
    nil))

(defn format-project-data [context request client projects]
  (let [api (wring/user-api request)]
    (map
      (fn [p]
        (let [project-id (:id p)
              client-id (:id client)
              sows (format-sow-data api context client-id project-id)
              engagements (:result (api/find-all-engagements api {:project-id (:id p)}))]
          {:project-id project-id
           :project-name (:name p)
           :edit-url (urls/edit-project-url context {:project-id project-id
                                                     :client-id client-id})
           :sows sows
           :delete-url (generate-delete-url-if-empty context project-id client-id sows engagements)
           :engagements engagements}))
      projects)))

(defn show-client [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:id params)]
    (when-status
      :success
      (fn [api client]
        (when-status
          :success
          (fn [api projects]
            (-> {:context context
                 :client client
                 :projects (format-project-data context request client projects)}
              build-view-data-for-show-client-view
              render-show-client-view
              response/response
              (wring/set-user-api api)))
          (api/find-all-projects-for-client api client-id)))
      (api/find-client-by-id api client-id))))
