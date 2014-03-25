(ns stockroom.admin.people.new-person
  (:require [ring.util.response :as response]
            [stockroom.admin.people.new-view :refer [render-new-person-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-new-view [{:keys [context params errors]}]
  {:create-person-url (urls/create-person-url context)
   :params params
   :errors errors})

(defn respond-with-new-person-view [{:keys [api response-status] :as options}]
  (-> options
    build-view-data-for-new-view
    render-new-person-view
    response/response
    (response/status response-status)
    (wring/set-user-api api)))

(defn new-person [context request]
  (respond-with-new-person-view {:context context
                                 :api (wring/user-api request)
                                 :response-status 200
                                 :errors {}
                                 :params (:params request)}))
