(ns stockroom.admin.location-memberships.new-location-membership
  (:require [stockroom.admin.location-memberships.form :refer :all]
            [stockroom.admin.location-memberships.new-view :refer [render-new-location-membership-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer :all]))

(defn build-view-data-for-new-location-membership-view [{:keys [context] :as options} employment-id]
  (merge {:create-location-membership-url (urls/create-location-membership-url context {:employment-id employment-id})}
         (build-view-data-for-location-membership-form options)))

(defn respond-with-new-location-membership-view [{:keys [context] :as options}]
  (respond-with-location-membership-form
    (fn [{:keys [employment-id]} form-body]
      (-> (build-view-data-for-new-location-membership-view options employment-id)
          (render-new-location-membership-view form-body)))
    options))

(defn new-location-membership [context request]
  (respond-with-new-location-membership-view {:context context
                                              :request request
                                              :errors {}
                                              :response-status 200}))