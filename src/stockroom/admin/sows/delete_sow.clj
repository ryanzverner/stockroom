(ns stockroom.admin.sows.delete-sow
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn delete-sow [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [client-id sow-id]} params]
    (when-status
      :success
      (fn [api _]
        (-> (response/redirect-after-post
              (urls/show-client-url context {:client-id client-id}))
          (assoc-in [:flash :success] "Removed SOW.")
          (wring/set-user-api api)))
      (api/delete-sow! api sow-id))))