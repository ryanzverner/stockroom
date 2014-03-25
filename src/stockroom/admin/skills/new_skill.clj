(ns stockroom.admin.skills.new-skill
  (:require [ring.util.response :as response]
            [stockroom.admin.skills.new-view :refer [render-new-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-new-skill-view [{:keys [context errors]}]
  {:create-skill-url (urls/create-skill-url context)
   :errors errors})

(defn respond-with-new-skill-view [{:keys [response-status request] :as options}]
  (-> options
    build-view-data-for-new-skill-view
    render-new-skill-view
    response/response
    (response/status response-status)
    (wring/set-user-api (wring/user-api request))))

(defn new-skill [context request]
  (respond-with-new-skill-view {:context context
                                 :request request
                                 :response-status 200
                                 :errors {}}))
