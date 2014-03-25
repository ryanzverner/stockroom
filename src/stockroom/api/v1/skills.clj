(ns stockroom.api.v1.skills
  (:require [ring.util.response :as response]
            [stockroom.api.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn list-skills [request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api skills]
        (response/response {:skills skills}))
      (api/find-all-skills api))))

(defn show-skill [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        skill-id (:skill-id params)]
    (when-status
      :success
      (fn [api skill]
        (response/response skill))
      (api/find-skill-by-id api skill-id))))

(defn create-skill [{:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api skill-id]
        (-> (response/response skill-id)
          (response/status 201)
          (wring/set-user-api api)))
      (api/create-skill! api params))))

(defn update-skill [{:keys [params] :as request}]
  (let [api (wring/user-api request)
        skill-id (:skill-id params)]
    (when-status
      :success
      (fn [api _]
        (-> (response/response "")
          (wring/set-user-api api)))
      (api/update-skill! api skill-id params))))
