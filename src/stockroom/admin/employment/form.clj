(ns stockroom.admin.employment.form
  (:require [metis.core :refer [defvalidator]]
            [metis.util :refer [present?]]
            [ring.util.response :as response]
            [stockroom.admin.employment.form-view :refer [render-employment-form-view]]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors
                                                   when-status]]
            [stockroom.admin.util.view-helper :refer :all]
            [stockroom.admin.form-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def missing-start-date-error "Please enter a start date.")
(def missing-position-id-error "Please select a position.")
(def invalid-position-id-error missing-position-id-error)
(def missing-person-id-error "Please select a person.")
(def invalid-person-id-error missing-person-id-error)
(def missing-location-id-error "Please select a location.")
(def invalid-location-id-error missing-location-id-error)
(def invalid-date-error "Please enter a date with the format yyyy-mm-dd")

(def employment-api-error-mapping
  {:employment/invalid-position-id {:key :position-id
                                    :message invalid-position-id-error}
   :employment/invalid-person-id {:key :person-id
                                  :message invalid-person-id-error}})

(defn translate-employment-api-errors-to-web-errors [errors]
  (api-errors-to-web-errors errors employment-api-error-mapping))

(defvalidator
  validate-employment-form
  [:start [:presence {:message missing-start-date-error}
           :with {:validator #(date-from-input (:start %))
                  :message invalid-date-error
                  :if #(present? (:start %))}]]
  [:end [:with {:validator #(date-from-input (:end %))
                :message invalid-date-error
                :if #(present? (:end %))}]]
  [:position-id [:presence {:message missing-position-id-error}]]
  [:person-id [:presence {:message missing-person-id-error}]]

  [:location-id [:presence {:if-not #(present? (:employment-id %))
                            :message missing-location-id-error}]])

(defn form-params->employment [params]
  {:position-id (:position-id params)
   :person-id   (:person-id params)
   :start       (date-from-input (:start params))
   :end         (date-from-input (:end params))
   :location-id (:location-id params)})

(defn employment->form-params [employment params]
  {:start (or (:start params) (date-for-input (:start employment)))
   :end   (or (:end params) (date-for-input (:end employment)))
   :person-id (or (:person-id params) (str (:person-id employment)))
   :position-id (or (:position-id params) (str (:position-id employment)))})

(defn build-view-data-for-employment-form [{:keys [context errors params positions people locations location-memberships]}]
  {:errors errors
   :params params
   :position-options (position-select-options positions)
   :person-options (person-select-options people)
   :location-options (location-select-options locations)
   :location-memberships location-memberships})

(defn respond-with-employment-form [view-fn {:keys [context errors request params response-status]}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api positions]
        (when-status
          :success
          (fn [api people]
            (when-status
              :success
              (fn [api locations]
                (let [form-view-data {:errors errors
                                      :params params
                                      :positions positions
                                      :people people
                                      :locations locations}
                      form-body (-> form-view-data
                                  build-view-data-for-employment-form
                                  render-employment-form-view)]
                  (-> (view-fn form-view-data form-body)
                    response/response
                    (response/status response-status)
                    (wring/set-user-api api))))
              (api/find-all-locations api)))
          (api/find-all-people api)))
      (api/find-all-employment-positions api))))
