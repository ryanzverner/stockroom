(ns stockroom.api.v1.employments
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :as util-response]
            [stockroom.api.v1.format :refer [parse-date-from-web]]
            [stockroom.v1.api :as api]
            [stockroom.api.v1.dates :refer [parse-dates]]
            [stockroom.api.v1.format :refer [maybe-format-date
                                             parse-date-from-web]]
            [stockroom.v1.ring :as wring]))

(defn params->employment [{:keys [person-id position-id location-id position-name]} {:keys [start end]}]
  {:person-id   person-id
   :position-id position-id
   :location-id location-id
   :position-name position-name
   :start       start
   :end         end})

(defn parse-index-params [{:keys [location-id start-date end-date]}]
  (let [errors []
        results {:location-id location-id}
        [results errors] (if start-date
                           (if-let [date (parse-date-from-web start-date)]
                             [(assoc results :start-date date) errors]
                             [results (conj errors util-response/employments-malformatted-start-date)])
                           [results errors])
        [results errors] (if end-date
                           (if-let [date (parse-date-from-web end-date)]
                             [(assoc results :end-date date) errors]
                             [results (conj errors util-response/employments-malformatted-end-date)])
                           [results errors])]
    [results errors]))

(defn list-employments [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        [parsed-params errors] (parse-index-params params)]
    (util-response/when-status
      :success
      (fn [api employments]
        (response/response {:employments employments}))
      (api/find-all-employments api {:location-id (:location-id parsed-params)
                                     :start-date (:start-date parsed-params)
                                     :end-date (:end-date parsed-params)}))))

(defn create-employment [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        [results errors] (parse-dates params
                                      util-response/employments-malformatted-start-date
                                      util-response/employments-malformatted-end-date)]
    (if (seq errors)
      (util-response/failure-response api errors)
      (util-response/when-status
        :success
        (fn [api employment-id]
          (-> (response/response employment-id)
              (response/status 201)
              (wring/set-user-api api)))
        (api/create-employment! api (params->employment params results))))))

(defn format-employment-for-web [employment]
  (-> employment
    (maybe-format-date :start)
    (maybe-format-date :end)))

(defn show-employment [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        employment-id (:employment-id params)]
    (util-response/when-status
      :success
      (fn [api employment]
        (-> (format-employment-for-web employment)
          response/response
          (wring/set-user-api api)))
      (api/find-employment-by-id api employment-id))))

(defn update-employment [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        employment-id (:employment-id params)
        [results errors] (parse-dates params
                                      util-response/employments-malformatted-start-date
                                      util-response/employments-malformatted-end-date)]

    (if (seq errors)
      (util-response/failure-response api errors)
      (util-response/when-status
        :success
        (fn [api _]
          (-> (response/response "")
            (wring/set-user-api api)))
        (api/update-employment! api employment-id (params->employment params results))))))
