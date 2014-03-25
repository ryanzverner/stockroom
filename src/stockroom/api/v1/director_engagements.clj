(ns stockroom.api.v1.director-engagements
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer :all]
            [stockroom.api.v1.format :refer [maybe-format-date
                                             parse-date-from-web]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn params->director-engagement [params {:keys [start end]}]
  (into {} (filter
             #(contains? params (first %))
             {:person-id (:person-id params)
              :project-id (:project-id params)
              :start start
              :end end})))

(defn parse-director-engagement-dates [{:keys [start end]}]
  (let [errors []
        results {}
        [results errors] (if start
                           (if-let [date (parse-date-from-web start)]
                             [(assoc results :start date) errors]
                             [results (conj errors director-engagements-malformatted-start-date)])
                           [results errors])
        [results errors] (if end
                           (if-let [date (parse-date-from-web end)]
                             [(assoc results :end date) errors]
                             [results (conj errors director-engagements-malformatted-end-date)])
                           [results errors])]
    [results errors]))

(defn update-director-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        director-engagement-id (:director-engagement-id params)
        [results errors] (parse-director-engagement-dates params)]
    (if (seq errors)
      (failure-response api errors)
      (when-status
        :success
        (fn [api _]
          (-> (response/response "")
            (wring/set-user-api api)))
        (api/update-director-engagement! api director-engagement-id (params->director-engagement params results))))))

(defn create-director-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        [results errors] (parse-director-engagement-dates params)]
    (if (seq errors)
      (failure-response api errors)
      (when-status
        :success
        (fn [api director-engagement-id]
          (-> (response/response director-engagement-id)
            (response/status 201)
            (wring/set-user-api api)))
        (api/create-director-engagement! api (params->director-engagement params results))))))

(defn format-director-engagement-for-web [director-engagement]
  (-> director-engagement
    (maybe-format-date :start)
    (maybe-format-date :end)))

(defn show-director-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        director-engagement-id (:director-engagement-id params)]
    (when-status
      :success
      (fn [api director-engagement]
        (-> (format-director-engagement-for-web director-engagement)
          response/response
          (wring/set-user-api api)))
      (api/find-director-engagement-by-id api director-engagement-id))))

