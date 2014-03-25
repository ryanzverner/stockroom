(ns stockroom.api.v1.directors
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :as api-response]
            [stockroom.api.v1.format :refer [maybe-format-date]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn format-dates-for-web [director-engagement]
  (-> director-engagement
    (maybe-format-date :start)
    (maybe-format-date :end)))

(defn list-current-directors [request]
  (let [api (wring/user-api request)]
    (api-response/when-status
      :success
      (fn [api directors]
        (-> {:directors directors}
          response/response
          (wring/set-user-api api)))
      (api/find-current-directors api))))

(defn list-director-engagements [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        director-id (:director-id params)]
    (api-response/when-status
      :success
      (fn [api director-engagements]
        (-> {:director-engagements (map format-dates-for-web director-engagements)}
          response/response
          (wring/set-user-api api)))
      (api/find-all-director-engagements-by-person-id api director-id))))

