(ns stockroom.admin.clients.new-client
  (:require [ring.util.response :as response]
            [stockroom.admin.clients.new-view :refer [render-new-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-new-client-view [{:keys [context errors]}]
  {:create-client-url (urls/create-client-url context)
   :errors errors})

(defn respond-with-new-client-view [{:keys [response-status request] :as options}]
  (-> options
    build-view-data-for-new-client-view
    render-new-client-view
    response/response
    (response/status response-status)
    (wring/set-user-api (wring/user-api request))))

(defn new-client [context request]
  (respond-with-new-client-view {:context context
                                 :request request
                                 :response-status 200
                                 :errors {}}))
