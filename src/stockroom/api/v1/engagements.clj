(ns stockroom.api.v1.engagements
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer :all]
            [stockroom.api.v1.format :refer [maybe-format-date
                                             parse-date-from-web]]
            [stockroom.api.util.response :refer [engagements-malformatted-start-date
                                                 engagements-malformatted-end-date
                                                 engagements-start-end-xnor-required]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn params->engagement [{:keys [employment-id project-id confidence-percentage]} {:keys [start end]}]
  {:employment-id employment-id
   :project-id project-id
   :start start
   :end end
   :confidence-percentage confidence-percentage})

(defn parse-engagement-dates [{:keys [start end]}]
  (let [errors []
        results {}
        [results errors] (if start
                           (if-let [date (parse-date-from-web start)]
                             [(assoc results :start date) errors]
                             [results (conj errors engagements-malformatted-start-date)])
                           [results errors])
        [results errors] (if end
                           (if-let [date (parse-date-from-web end)]
                             [(assoc results :end date) errors]
                             [results (conj errors engagements-malformatted-end-date)])
                           [results errors])]
    [results errors]))

(defn create-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        [results errors] (parse-engagement-dates params)]
    (if (seq errors)
      (failure-response api errors)
      (when-status
        :success
        (fn [api engagement-id]
          (-> (response/response engagement-id)
            (response/status 201)
            (wring/set-user-api api)))
        (api/create-engagement! api (params->engagement params results))))))

(defn update-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        engagement-id (:engagement-id params)
        [results errors] (parse-engagement-dates params)]
    (if (seq errors)
      (failure-response api errors)
      (when-status
        :success
        (fn [api _]
          (-> (response/response "")
            (wring/set-user-api api)))
        (api/update-engagement! api engagement-id (params->engagement params results))))))

(defn delete-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        engagement-id (:engagement-id params)]
    (when-status
      :success
      (fn [api _]
        (-> (response/response "")
          (wring/set-user-api api)))
      (api/delete-engagement! api engagement-id))))

(defn maybe-parse-date [data key value]
  (if-let [parsed-date (parse-date-from-web value)]
    (assoc data key parsed-date)
    (dissoc data key)))

(defn parse-index-params [{:keys [start end project-id]}]
  (let [errors []
        results {:project-id project-id}
        [results errors] (if (or
                               (and start (not end))
                               (and end (not start)))
                           [results (conj errors engagements-start-end-xnor-required)]
                           [results errors])
        [results errors] (if start
                           (if-let [date (parse-date-from-web start)]
                             [(assoc results :start date) errors]
                             [results (conj errors engagements-malformatted-start-date)])
                           [results errors])
        [results errors] (if end
                           (if-let [date (parse-date-from-web end)]
                             [(assoc results :end date) errors]
                             [results (conj errors engagements-malformatted-end-date)])
                           [results errors])]
    [results errors]))

(defn format-engagement-for-web [engagement]
  (-> engagement
    (maybe-format-date :start)
    (maybe-format-date :end)
    (maybe-format-date :created-at)
    (maybe-format-date :updated-at)))

(defn format-project-for-web [project]
  (-> project
    (maybe-format-date :created-at)
    (maybe-format-date :updated-at)
    (maybe-format-date :start)
    (maybe-format-date :end)))

(defn format-person-for-web [person]
  (-> person
    (maybe-format-date :created-at)
    (maybe-format-date :updated-at)))

(defn format-engagement-and-children-for-web [engagement]
  (-> engagement
      (format-engagement-for-web)
      (assoc :person (format-person-for-web (:person engagement)))
      (assoc :project (format-project-for-web (:project engagement)))))

(defn list-engagements [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        project-id-provided? (not (nil? (:project-id params)))
        [parsed-params errors] (parse-index-params params)]
    (if (seq errors)
      (bad-request-response api errors)
      (when-status
        :success
        (fn [api engagements]
          (-> {:engagements (map format-engagement-and-children-for-web engagements)}
              response/response
              (wring/set-user-api api)))
        (api/find-all-engagements api parsed-params)))))

(defn show-engagement [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        engagement-id (:engagement-id params)]
    (when-status
      :success
      (fn [api engagement]
        (-> (format-engagement-for-web engagement)
          response/response
          (wring/set-user-api api)))
      (api/find-engagement-by-id api engagement-id))))
