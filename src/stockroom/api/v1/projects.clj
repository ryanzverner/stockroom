(ns stockroom.api.v1.projects
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn list-projects [request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api projects]
        (response/response {:projects projects}))
      (api/find-all-projects api {:sort :updated-at :direction :desc}))))

(defn show-project [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        project-id (:project-id params)]
    (when-status
      :success
      (fn [api project]
        (response/response project))
      (api/find-project-by-id api project-id))))

(defn create-project [{:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api project-id]
        (-> (response/response project-id)
            (response/status 201)
            (wring/set-user-api api)))
      (api/create-project! api params))))

(defn update-project [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        project-id (:project-id params)]
    (when-status
      :success
      (fn [api _]
        (-> (response/response "")
            (wring/set-user-api api)))
      (api/update-project! api project-id params))))

