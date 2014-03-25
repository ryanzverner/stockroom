(ns stockroom.admin.employment.edit-employment
  (:require [stockroom.admin.employment.edit-view :refer [render-edit-employment-view]]
            [stockroom.admin.employment.form :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.admin.util.view-helper :refer [month-day-year]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn- get-location-name [api id]
  (:name (:result (api/find-location-by-id api id))))

(defn- create-location-membership-data [api {:keys [location-id start id]}]
  {:location (get-location-name api location-id)
   :start (month-day-year start)
   :location-membership-id id})

(defn format-location-membership-data [location-memberships request]
  (let [api (wring/user-api request)]
    (map #(create-location-membership-data api %) location-memberships)))

(defn build-view-data-for-edit-employment-view [{:keys [context employment location-memberships] :as options}]
  (merge {:update-employment-url (urls/update-employment-url context {:employment-id (:id employment)})
          :delete-location-membership-url (urls/delete-location-membership-url context {:employment-id (:id employment)})
          :new-location-membership-url (urls/new-location-membership-url context {:employment-id (:id employment)})}
         (build-view-data-for-employment-form options)))

(defn respond-with-edit-employment-view [{:keys [context request errors response-status] :as options}]
  (let [api (wring/user-api request)
        params (:params request)
        employment-id (:employment-id params)]
    (when-status
      :success
      (fn [api location-memberships]
        (when-status
          :success
          (fn [api employment]
            (respond-with-employment-form
              (fn [form-view-data form-body]
                (-> {:context context :employment employment :location-memberships (format-location-membership-data location-memberships request)}
                  build-view-data-for-edit-employment-view
                  (render-edit-employment-view form-body)))
              {:context context
               :request request
               :params (employment->form-params employment params)
               :errors errors
               :employment employment
               :response-status response-status}))
        (api/find-employment-by-id api employment-id)))
      (api/find-all-location-memberships-for-employment api employment-id))))

(defn edit-employment [context request]
  (respond-with-edit-employment-view {:context context
                                      :request request
                                      :errors {}
                                      :response-status 200}))
