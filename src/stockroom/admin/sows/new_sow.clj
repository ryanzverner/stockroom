(ns stockroom.admin.sows.new-sow
  (:require [ring.util.response :as response]
            [stockroom.admin.sows.new-view :refer [render-new-sow-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def currency-code-options [["USD" "USD"] ["GBP" "GBP"] ["EUR" "EUR"]])
(def default-currency-code "USD")

(defn build-view-data-for-new-sow-view [{:keys [context errors params client projects]}]
  {:create-sow-url (urls/create-sow-url context {:client-id (:id client)})
   :errors errors
   :params (assoc params :currency-code default-currency-code)
   :projects projects
   :currency-code-options currency-code-options})

(defn respond-with-new-sow-view [{:keys [request context client-id params errors response-status]}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api projects]
        (when-status
          :success
          (fn [api client]
            (-> {:context context :errors errors :params params :client client :projects projects}
              build-view-data-for-new-sow-view
              render-new-sow-view
              response/response
              (response/status response-status)))
          (api/find-client-by-id api client-id)))
      (api/find-all-projects-for-client api client-id))))

(defn new-sow [context request]
  (respond-with-new-sow-view {:context context
                              :request request
                              :response-status 200
                              :client-id (-> request :params :client-id)
                              :params (:params request)
                              :errors {}}))
