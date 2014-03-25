(ns stockroom.api.v1.locations
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn list-locations [request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api locations]
        (response/response {:locations locations}))
      (api/find-all-locations api))))

(defn show-location [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        location-id (:location-id params)]
    (when-status
      :success
      (fn [api location]
        (response/response location))
      (api/find-location-by-id api location-id))))

(defn create-location [{:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api location-id]
        (-> (response/response location-id)
          (response/status 201)
          (wring/set-user-api api)))
      (api/create-location! api params))))
