(ns stockroom.admin.clients.delete-client
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn delete-client [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [client-id]} params]
    (when-status
      :success
      (fn [api _]
        (-> (response/redirect-after-post
              (urls/list-clients-url context))
          (assoc-in [:flash :success] "Removed client.")
          (wring/set-user-api api)))
      (api/delete-client! api client-id))))