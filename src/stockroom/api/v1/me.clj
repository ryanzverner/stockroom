(ns stockroom.api.v1.me
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :as api-response]
            [stockroom.api.util.request :as api-request]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn list-my-permissions [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        user-id (api-request/current-user-id request)]
    (api-response/when-status
      :success
      (fn [api permissions]
        (response/response permissions))
      (api/find-all-permissions-for-user api user-id))))
