(ns stockroom.admin.sows.form
  (:require [metis.core :refer [defvalidator]]
            [metis.util :refer [present?]]
            [ring.util.response :as response]
            [stockroom.admin.sows.form-view :refer [render-sow-form-view]]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors
                                                   when-status]]
            [stockroom.admin.util.view-helper :refer :all]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def missing-start-date-error "Please enter a start date.")
(def missing-hourly-rate-error "Please enter an hourly rate.")
(def invalid-date-error "Please enter a date with the format yyyy-mm-dd")
(def no-project-selected-error "Please select at least one project.")
(def currency-code-options [["USD" "USD"] ["GBP" "GBP"] ["EUR" "EUR"]])

(defn form-params->sow [params]
  {:start           (date-from-input (:start params))
   :end             (date-from-input (:end params))
   :hourly-rate     (:hourly-rate params)
   :currency-code   (:currency-code params)
   :url             (:url params)
   :signed-date     (date-from-input (:signed-date params))
   :projects        (:projects params)})

(defn sow->form-params [sow params]
  {:start (or (:start params) (date-for-input (:start sow)))
   :end   (or (:end params) (date-for-input (:end sow)))
   :hourly-rate (or (:hourly-rate params) (str (:hourly-rate sow)))
   :currency-code (or (:currency-code params) (str (:currency-code sow)))
   :signed-date (or (:signed-date params) (date-for-input (:signed-date sow)))
   :url (or (:url params) (str (:url sow)))})

(defn set-checked [client-project sow-projects]
  (let [client-project-id (:id client-project)
        sow-projects-project-ids (map :id sow-projects)
        checked (boolean (some #{client-project-id} sow-projects-project-ids))]
    (assoc client-project :checked checked)))

(defn build-view-data-for-sow-form [{:keys [errors params projects sow-projects]}]
  {:errors errors
   :params params
   :projects (map #(set-checked % sow-projects) projects)
   :currency-code-options currency-code-options})

(defn respond-with-sow-form [view-fn {:keys [request context params errors response-status]}]
  (let [api (wring/user-api request)
        client-id (:client-id (:params request))
        sow-id (:sow-id (:params request))]
    (when-status
      :success
      (fn [api projects-for-client]
        (when-status
          :success
          (fn [api projects-for-sow]
            (when-status
              :success
              (fn [api client]
                (let [form-view-data {:errors errors
                                      :params params
                                      :projects projects-for-client
                                      :sow-projects projects-for-sow}
                      form-body (-> form-view-data
                                    build-view-data-for-sow-form
                                    render-sow-form-view)]
                  (-> (view-fn form-view-data form-body)
                    response/response
                    (response/status response-status)
                    (wring/set-user-api api))))
              (api/find-client-by-id api client-id)))
          (api/find-all-projects api {:sow-id sow-id})))
      (api/find-all-projects-for-client api client-id))))

(defvalidator validate-sow-form
  [:start [:presence {:message missing-start-date-error}
           :with {:validator #(date-from-input (:start %))
                  :message invalid-date-error
                  :if #(present? (:start %))}]]
  [:end [:with {:validator #(date-from-input (:end %))
                :message invalid-date-error
                :if #(present? (:end %))}]]
  [:hourly-rate :presence {:message missing-hourly-rate-error}]
  [:projects [:presence {:message no-project-selected-error}]])
