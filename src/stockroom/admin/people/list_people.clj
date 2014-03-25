(ns stockroom.admin.people.list-people
  (:require [ring.util.response :as response]
            [stockroom.admin.people.index-view :refer [render-people-index-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-index-view [{:keys [people context]}]
  {:new-person-url (urls/new-person-url context)
   :people (map
             (fn [person]
               {:first-name (:first-name person)
                :last-name (:last-name person)
                :email (:email person)
                :edit-url (urls/edit-person-url context {:person-id (:id person)})})
             people)})

(defn list-people [context request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api people]
        (-> {:people people :context context}
          build-view-data-for-index-view
          render-people-index-view
          response/response
          (wring/set-user-api api)))
      (api/find-all-people api))))
