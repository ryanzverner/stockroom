(ns stockroom.v1.memory.people
  (:require [chee.datetime :as cd]
            [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]))

(defn create-person! [{:keys [db] :as api} people-data]
  (let [[db id] (helpers/insert db :people people-data)]
    (response/success (assoc api :db db) id)))

(defn update-person! [{:keys [db] :as api} person-id person-data]
  (if-let [person (helpers/find-by-id db :people person-id)]
    (let [finder #(= (:id %) person-id)
          updater (fn [_] person-data)
          db (helpers/update-where db :people finder updater)]
      (response/success (assoc api :db db) nil))
    (response/not-found api)))

(defn find-person-by-id [{:keys [db] :as api} person-id]
  (if-let [person (helpers/find-by-id db :people person-id)]
    (response/success api person)
    (response/not-found api)))

(defn find-all-people [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :people)
       (response/success api)))

(defn search-people [{:keys [db] :as api} criteria]
  (let [first-name (:first-name criteria)
        last-name (:last-name criteria)
        email (:email criteria)
        search-fn #(and (or (nil? first-name) (= first-name (:first-name %)))
                        (or (nil? last-name) (= last-name (:last-name %)))
                        (or (nil? email) (= email (:email %))))]
    (response/success api (helpers/find-where db :people search-fn))))

(defn latest-location-membership-start-date [{:keys [db] :as api} employment-id]
  (->> (helpers/all-data-for-type db :location-memberships {:namespace "location-memberships"})
       (filter (comp (partial = employment-id) :location-memberships/employment-id))
       (map :location-memberships/start)
       (sort)
       (last)))

(defn- distinct-people [people]
  (let [grouped-people (group-by #(:id %1) people)]
    (map first (vals grouped-people))))

(defn find-current-people-by-position [{:keys [db] :as api} position-name]
  (-> (helpers/all-data-for-type db :people {:namespace "people"})
      (helpers/join (helpers/all-data-for-type db :employment {:namespace "employment"})
        (fn [person employment]
          (= (:people/id person)
             (:employment/person-id employment))))
      (helpers/join (helpers/all-data-for-type db :positions {:namespace "positions"})
        (fn [employment postion]
          (= (:employment/position-id employment)
             (:positions/id postion))))
      (as-> people
        (filter
          #(let [employment (helpers/select-keys-with-ns % "employment")
                 position (helpers/select-keys-with-ns % "positions")]
            (and
              (or
                (= nil (:end employment))
                (cd/before? (cd/now) (:end employment)))
              (= (:name position) position-name)))
          people)
        (map #(helpers/select-keys-with-ns % "people") people)
        (distinct-people people)
        (response/success api people))))
