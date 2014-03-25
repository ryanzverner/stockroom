(ns stockroom.admin.users.new-user
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.new-view :refer [render-new-user-view]]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-user-new [{:keys [context errors]}]
  {:create-url-url (urls/create-user-url context)
   :errors errors})

(defn respond-with-new-user-view [{:keys [request response-status] :as options}]
  (-> options
    build-view-data-for-user-new
    render-new-user-view
    response/response
    (response/status response-status)
    (wring/set-user-api (wring/user-api request))))

(defn new-user [context request]
  (respond-with-new-user-view {:request request
                               :context context
                               :errors {}
                               :response-status 200}))
