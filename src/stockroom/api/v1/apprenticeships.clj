(ns stockroom.api.v1.apprenticeships
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer [when-status] :as util-response]
            [stockroom.api.v1.format :refer [parse-date-from-web]]
            [stockroom.v1.api :as api]
            [stockroom.api.v1.dates :refer [parse-dates]]
            [stockroom.v1.ring :as wring]))

(defn build-mentorship [attrs]
  {:person-id (:person-id attrs)
   :start (parse-date-from-web (:start attrs))
   :end (parse-date-from-web (:end attrs))})

(defn params->mentorships [params]
  {:mentorships (map build-mentorship (:mentorships params))})

(defn params->apprenticeship [params {:keys [start end]}]
  (merge {:person-id (:person-id params)
          :skill-level (:skill-level params)
          :start (parse-date-from-web (:start params))
          :end (parse-date-from-web (:end params))}
         (params->mentorships params)))

(defn- merge-current-locations-into-apprenticeships [apprenticeships current-locations]
  (map
    (fn [apprenticeship]
      (assoc-in
        apprenticeship
        [:person :current-location]
        (current-locations (:id (:person apprenticeship)))))
    apprenticeships))

(defn- apprentice-ids [apprenticeships]
  (map #(:id (:person %)) apprenticeships))

(defn list-apprenticeships [request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api apprenticeships]
          (response/response {:apprenticeships
            (->>
              (apprentice-ids apprenticeships)
              (api/find-current-location-membership-for-people api)
              :result
              (merge-current-locations-into-apprenticeships apprenticeships))}))
      (api/find-all-apprenticeships api))))

(defn show-apprenticeship [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        apprenticeship-id (:apprenticeship-id params)]
    (when-status
      :success
      (fn [api apprenticeship]
        (response/response apprenticeship))
      (api/find-apprenticeship-by-id api apprenticeship-id))))

(defn create-apprenticeship [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        [results errors] (parse-dates params
                                      util-response/apprenticeships-malformatted-start-date
                                      util-response/apprenticeships-malformatted-end-date)]
    (if (seq errors)
      (util-response/failure-response api errors)
      (util-response/when-status
        :success
        (fn [api apprenticeship-id]
          (-> (response/response apprenticeship-id)
              (response/status 201)
              (wring/set-user-api api)))
        (api/create-apprenticeship! api (params->apprenticeship params results))))))

(defn upcoming-apprentice-graduations-by-location [request]
  (when-status
    :success
    (fn [api result] (response/response result))
    (api/upcoming-apprentice-graduations-by-location (wring/user-api request))))
