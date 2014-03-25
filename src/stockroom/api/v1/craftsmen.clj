(ns stockroom.api.v1.craftsmen
  (:require
    [ring.util.response :as response]
    [stockroom.v1.api :as api]
    [stockroom.v1.ring :as wring]))

(defn list-current-craftsmen [request]
  (let [api (wring/user-api request)
        craftsmen (:result (api/find-current-people-by-position api "craftsman"))
        craftsmen-ids (map :id craftsmen)
        current-locations (:result (api/find-current-location-membership-for-people api craftsmen-ids))]
    (response/response
      (map (fn [craftsman]
        (assoc craftsman :current-location (current-locations (:id craftsman))))
        craftsmen))))
