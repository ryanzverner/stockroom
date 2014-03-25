(ns stockroom.admin.users.list-users
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.index-view :refer [render-users-index-view]]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-users-index-view [{:keys [context all-users]}]
  {:users (map
            (fn [u]
              {:url (urls/show-user-url context {:user-id (:id u)})
               :name (:name u)})
            all-users)
   :new-user-url (urls/new-user-url context)})

(defn list-users [context request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api users]
        (-> {:context context :all-users users}
          build-view-data-for-users-index-view
          render-users-index-view
          response/response
          (wring/set-user-api api)))
      (api/find-all-users api))))
