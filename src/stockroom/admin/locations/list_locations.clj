(ns stockroom.admin.locations.list-locations
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.locations.index-view :refer [render-locations-index-view]]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-locations-index-view [{:keys [context all-locations]}]
  {:locations (map
            (fn [u]
              {:url (urls/show-location-url context {:location-id (:id u)})
               :name (:name u)})
            all-locations)
   :new-location-url (urls/new-location-url context)})

(defn list-locations [context request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api locations]
        (-> {:context context :all-locations locations}
          build-view-data-for-locations-index-view
          render-locations-index-view
          response/response
          (wring/set-user-api api)))
      (api/find-all-locations api))))
