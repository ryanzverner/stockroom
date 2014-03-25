(ns stockroom.admin.location-memberships.form
  (:require [metis.core :refer [defvalidator]]
            [metis.util :refer [present?]]
            [ring.util.response :as response]
            [stockroom.admin.location-memberships.form-view :refer [render-location-membership-form-view]]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors
                                                   when-status]]
            [stockroom.admin.util.view-helper :refer :all]
            [stockroom.admin.form-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def missing-start-date-error "Please enter a start date.")
(def missing-location-id-error "Please select a location.")
(def invalid-location-id-error missing-location-id-error)
(def missing-employment-id-error "Please select a employment.")
(def invalid-employment-id-error missing-employment-id-error)
(def invalid-date-error "Please enter a date with the format yyyy-mm-dd")

(def location-membership-api-error-mapping
  {:location-membership/invalid-location-id {:key :location-id
                                    :message invalid-location-id-error}
   :location-membership/invalid-employment-id {:key :employment-id
                                  :message invalid-employment-id-error}})

(defn translate-location-membership-api-errors-to-web-errors [errors]
  (api-errors-to-web-errors errors location-membership-api-error-mapping))

(defvalidator
  validate-location-membership-form
  [:start [:presence {:message missing-start-date-error}
           :with {:validator #(date-from-input (:start %))
                  :message invalid-date-error
                  :if #(present? (:start %))}]]
  [:location-id [:presence {:message missing-location-id-error}]]
  [:employment-id [:presence {:message missing-employment-id-error}]])

(defn form-params->location-membership [params]
  {:location-id     (:location-id params)
   :employment-id   (:employment-id params)
   :start           (date-from-input (:start params))})

(defn location-membership->form-params [location-membership params]
  {:start (or (:start params) (date-for-input (:start location-membership)))
   :employment-id (or (:employment-id params) (str (:employment-id location-membership)))
   :location-id (or (:location-id params) (str (:location-id location-membership)))})

(defn build-view-data-for-location-membership-form [{:keys [employment-id errors params locations request]}]
    {:errors errors
     :params params
     :location-options (location-select-options locations)
     :employment-id employment-id})

(defn respond-with-location-membership-form [view-fn {:keys [context errors request params response-status employment-id]}]
  (let [api (wring/user-api request)]
      (when-status
        :success
        (fn [api locations]
              (let [form-view-data {:employment-id (get (get request :params) :employment-id)
                                    :errors errors
                                    :params params
                                    :locations locations
                                    :request request}
                    form-body (-> form-view-data
                                build-view-data-for-location-membership-form
                                render-location-membership-form-view)]
                (-> (view-fn form-view-data form-body)
                  response/response
                  (response/status response-status)
                  (wring/set-user-api api))))
        (api/find-all-locations api))))