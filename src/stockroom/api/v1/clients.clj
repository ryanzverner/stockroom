(ns stockroom.api.v1.clients
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn list-clients [request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api clients]
        (response/response {:clients clients}))
      (api/find-all-clients api))))

(defn show-client [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:client-id params)]
    (when-status
      :success
      (fn [api client]
        (response/response client))
      (api/find-client-by-id api client-id))))

(defn create-client [{:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api client-id]
        (-> (response/response client-id)
          (response/status 201)
          (wring/set-user-api api)))
      (api/create-client! api params))))

(defn update-client [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:client-id params)]
    (when-status
      :success
      (fn [api _]
        (-> (response/response "")
          (wring/set-user-api api)))
      (api/update-client! api client-id params))))


