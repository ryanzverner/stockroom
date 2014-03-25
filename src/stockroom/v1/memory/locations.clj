(ns stockroom.v1.memory.locations
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.validations :as validations]
            [stockroom.v1.response :as response]))

(defn create-location! [{:keys [db] :as api} location-data]
  (if-let [errors (seq (validations/validate-location location-data helpers/present? api))]
    (response/failure api errors)
    (if (helpers/find-by-name db :locations (:name location-data))
      (response/failure api [response/locations-duplicate-name])
      (let [[db id] (helpers/insert db :locations location-data)]
        (response/success (assoc api :db db) id)))))

(defn find-location-by-id [{:keys [db] :as api} location-id]
  (if-let [location (helpers/find-by-id db :locations location-id)]
    (response/success api location)
    (response/not-found api)))

(defn find-all-locations [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :locations)
       (response/success api)))