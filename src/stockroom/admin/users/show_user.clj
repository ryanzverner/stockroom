(ns stockroom.admin.users.show-user
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.show-view :refer [render-show-user-view]]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-user-show-view [{:keys [context user groups]}]
  (let [return-url (urls/show-user-url context {:user-id (:id user)})]
    {:name (:name user)
     :groups (map
               (fn [g]
                 {:name (:name g)
                  :remove-url (urls/remove-user-from-group-url context {:group-id (:id g)})
                  :remove-params [{:name "user-id" :value (:id user)}
                                  {:name "return-url" :value return-url}]})
               groups)}))

(defn show-user [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        user-id (:id params)]
    (when-status
      :success
      (fn [api user]
        (when-status
          :success
          (fn [api groups]
            (-> {:context context :user user :groups groups}
              build-view-data-for-user-show-view
              render-show-user-view
              response/response
              (wring/set-user-api api)))
          (api/find-all-groups-for-user api user-id)))
      (api/find-user-by-id api user-id))))
