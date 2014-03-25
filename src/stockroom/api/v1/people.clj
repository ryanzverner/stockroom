(ns stockroom.api.v1.people
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :as api-response]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-person-params [{:keys [first-name last-name]}]
  (let [errors []
        errors (if (empty? first-name) (conj errors api-response/people-missing-first-name) errors)
        errors (if (empty? last-name)  (conj errors api-response/people-missing-last-name)  errors)]
    errors))

(defn create-person [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-person-params params)]
    (if (seq errors)
      (api-response/failure-response api errors)
      (api-response/when-status
        :success
        (fn [api person-id]
          (-> (response/response person-id)
            (response/status 201)
            (wring/set-user-api api)))
        (api/create-person! api params)))))

(defn search-people [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        search-params (select-keys params [:first-name, :last-name, :email])]
    (if (empty? search-params)
      (api-response/failure-response api api-response/person-missing-search-parameters)
      (api-response/when-status
        :success
        (fn [api people]
          (-> {:people people}
            response/response
            (wring/set-user-api api)))
        (api/search-people api search-params)))))

(defn show-person [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        person-id (:person-id params)]
    (api-response/when-status
      :success
      (fn [api person]
        (response/response person))
      (api/find-person-by-id api person-id))))

