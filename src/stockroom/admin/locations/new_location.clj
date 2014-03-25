(ns stockroom.admin.locations.new-location
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.locations.new-view :refer [render-new-location-view]]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-location-new [{:keys [context errors]}]
  {:create-location-url (urls/create-location-url context)
   :errors errors})

(defn respond-with-new-location-view [{:keys [request response-status] :as options}]
  (-> options
    build-view-data-for-location-new
    render-new-location-view
    response/response
    (response/status response-status)
    (wring/set-user-api (wring/user-api request))))

(defn new-location [context request]
  (respond-with-new-location-view {:request request
                                   :context context
                                   :errors {}
                                   :response-status 200}))