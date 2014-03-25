(ns stockroom.v1.memory.location-memberships
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.validations :as validations]
            [stockroom.v1.response :as response]))

(defn create-location-membership! [{:keys [db] :as api} employment-id location-id location-membership-data]
  (let [location-membership-data (assoc location-membership-data
                              :employment-id employment-id
                              :location-id location-id)
        [db id] (helpers/insert db :location-memberships location-membership-data)]
    (if-let [errors (seq (validations/validate-location-membership location-membership-data helpers/present? api))]
      (response/failure api errors)
      (response/success (assoc api :db db) id))))

(defn find-location-membership-by-id [{:keys [db] :as api} location-membership-id]
  (if-let [location-membership (helpers/find-by-id db :location-memberships location-membership-id)]
    (response/success api location-membership)
    (response/not-found api)))

(defn- map-person-id-to-current-location [person-id-to-location-memberships]
  (reduce
    (fn [acc person-with-locations]
      (let [person-id (nth person-with-locations 0)
            location-memberships (nth person-with-locations 1)
            current-location-membership (->> location-memberships
                                             (sort-by :location-memberships/start)
                                             last)
            data (assoc acc person-id {:id (:locations/id current-location-membership)
                            :name (:locations/name current-location-membership)})]
      (assoc acc person-id {:id (:locations/id current-location-membership)
                            :name (:locations/name current-location-membership)})))
      {}
      person-id-to-location-memberships))

(defn- join-people-to-employment [left db]
  (helpers/join left (helpers/all-data-for-type db :employment {:namespace "employment"})
    (fn [person employment]
      (= (:people/id person)
         (:employment/person-id employment)))))

(defn- join-employment-to-location-memberships [left db]
  (helpers/join left (helpers/all-data-for-type db :location-memberships {:namespace "location-memberships"})
    (fn [employment location-membership]
      (= (:employment/id employment)
         (:location-memberships/employment-id location-membership)))))

(defn- join-location-memberships-to-locations [left db]
  (helpers/join left (helpers/all-data-for-type db :locations {:namespace "locations"})
    (fn [location-membership location]
      (= (:location-memberships/location-id location-membership)
         (:locations/id location)))))

(defn- find-matching-people [person-ids person-id-to-location-memberships]
  (filter
    (fn [[person-id _]]
      (some #{person-id} person-ids))
    person-id-to-location-memberships))

(defn find-current-location-membership-for-people [{:keys [db] :as api} person-ids]
  (->
    (helpers/all-data-for-type db :people {:namespace "people"})
    (join-people-to-employment db)
    (join-employment-to-location-memberships db)
    (join-location-memberships-to-locations db)
    (as-> people
      (group-by :people/id people)
      (find-matching-people person-ids people)
      (map-person-id-to-current-location people)
      (response/success api people))))

(defn delete-location-membership! [{:keys [db] :as api} location-membership-id]
  (if-let [location-membership (helpers/find-by-id db :location-memberships location-membership-id)]
    (let [finder #(= (:id %) location-membership-id)
          db (helpers/remove-where db :location-memberships finder)]
      (response/success (assoc api :db db) nil))
    (response/not-found api)))

