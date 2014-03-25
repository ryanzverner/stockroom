(ns stockroom.admin.location-memberships.delete-location-membership
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]
            ))

(defn delete-location-membership [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [employment-id location-membership-id return-url]} params]
    (when-status
      :success
      (fn [api _]
        (-> (response/redirect-after-post
              (or return-url (urls/edit-employment-url context {:employment-id employment-id})))
          (assoc-in [:flash :success] "Removed location association.")
          (wring/set-user-api api)))
      (api/delete-location-membership! api location-membership-id))))