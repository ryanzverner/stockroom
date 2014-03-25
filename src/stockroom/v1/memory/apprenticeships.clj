(ns stockroom.v1.memory.apprenticeships
  (:require [chee.datetime :as cd]
            [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]
            [stockroom.v1.memory.people :as people]))

(defn create-apprenticeship! [{:keys [db] :as api} apprenticeship-data]
  (let [errors (validations/validate-apprenticeship apprenticeship-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (let [[db id] (helpers/insert db :apprenticeships apprenticeship-data)]
        (response/success (assoc api :db db) id)))))

(defn find-apprenticeship-by-id [{:keys [db] :as api} apprenticeship-id]
  (if-let [apprenticeship (helpers/find-by-id db :apprenticeships apprenticeship-id)]
    (let [person (helpers/find-by-id db :people (:person-id apprenticeship))
          mentorships (map (fn [m]
                             (assoc m :person (helpers/find-by-id db :people (:person-id m))))
                           (:mentorships apprenticeship))]
      (response/success api (assoc apprenticeship
                                   :person person
                                   :mentorships mentorships)))
    (response/not-found api)))

(defn find-all-apprenticeships [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :apprenticeships)
       (map (fn [a]
              (:result (find-apprenticeship-by-id api (:id a)))))
       (sort-by :id)
       (response/success api)))

(defn upcoming-apprentice-graduations-by-location [{:keys [db] :as api}]
  (let [all-locations (->> (helpers/all-data-for-type db :locations {:namespace "locations"})
                           (map (juxt :locations/name (constantly #{})))
                           (into {}))]
    (as-> db __
      (helpers/all-data-for-type __ :people {:namespace "people"})
      (helpers/join __
                    (helpers/all-data-for-type db :employment {:namespace "employment"})
                    (fn [l r]
                      (= (:people/id l)
                         (:employment/person-id r))))
      (helpers/join __
                    (helpers/all-data-for-type db :location-memberships {:namespace "location-memberships"})
                    (fn [l r]
                      (= (:employment/id l)
                         (:location-memberships/employment-id r))))
      (helpers/join __
                    (helpers/all-data-for-type db :locations {:namespace "locations"})
                    (fn [l r]
                      (= (:location-memberships/location-id l)
                         (:locations/id r))))
      (helpers/join __
                    (helpers/all-data-for-type db :apprenticeships {:namespace "apprenticeships"})
                    (fn [l r]
                      (= (:people/id l)
                         (:apprenticeships/person-id r))))
      (filter (fn [row]
                (and (= (people/latest-location-membership-start-date api (:location-memberships/employment-id row))
                        (:location-memberships/start row))
                     (cd/before? (cd/now) (:apprenticeships/end row))))
              __)
      (map (fn [row]
             {:first-name (:people/first-name row)
              :last-name (:people/last-name row)
              :graduates-at (:apprenticeships/end row)
              :location-name (:locations/name row)})
           __)
      (group-by :location-name __)
      (merge all-locations __)
      (map (fn [[location-name people]]
             {:location-name location-name
              :current-apprentices (->> people
                                        (map (fn [person]
                                               (dissoc person :location-name)))
                                        (set))})
           __)
      (set __)
      (response/success api __))))
