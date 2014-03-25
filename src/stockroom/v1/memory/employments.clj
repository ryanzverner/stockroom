(ns stockroom.v1.memory.employments
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]
            [stockroom.util.time :as date]
            [chee.datetime :refer [before? after? before days] :as datetime]))

(defn create-employment-position! [{:keys [db] :as api} position-data]
  (let [[db id] (helpers/insert db :positions position-data)]
    (response/success (assoc api :db db) id)))

(defn find-all-employment-positions [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :positions)
       (response/success api)))

(defn find-employment-position-by-id [{:keys [db] :as api} position-id]
  (if-let [position (helpers/find-by-id db :positions position-id)]
    (response/success api position)
    (response/not-found api)))

(defn find-employment-position-by-name [{:keys [db] :as api} position-name]
  (if-let [position (helpers/find-by-name db :positions position-name)]
    (response/success api position)
    (response/not-found api)))

(defn make-location-membership-data-from-employment-data [employment-data employment-id]
  (assoc {} :employment-id employment-id
            :location-id (:location-id employment-data)
            :start (:start employment-data)))

(defn create-employment! [{:keys [db] :as api} employment-data]
  (let [[employment-data errors] (validations/validate-employment-with-location employment-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (let [[db employment-id] (helpers/insert db :employment employment-data)
            location-membership-data (make-location-membership-data-from-employment-data employment-data employment-id)
            [db location-membership-id] (helpers/insert db :location-memberships location-membership-data)]
        (response/success (assoc api :db db) employment-id)))))

(defn update-employment! [{:keys [db] :as api} employment-id employment-data]
  (if-let [employment (helpers/find-by-id db :employment employment-id)]
    (let [employment-data (merge employment employment-data)
          [employment-data errors] (validations/validate-employment employment-data helpers/present? api)]
      (if (seq errors)
        (response/failure api errors)
        (let [finder #(= (:id %) employment-id)
              updater (fn [_] employment-data)
              db (helpers/update-where db :employment finder updater)]
          (response/success (assoc api :db db) nil))))
    (response/not-found api)))

(defn join-location-memberships [{:keys [db] :as api} employments location-id]
  (filter
    #(= location-id (:location-memberships/location-id %))
    (helpers/join
      employments
      (helpers/all-data-for-type db :location-memberships {:namespace "location-memberships"})
      (fn [employment location-membership]
        (= (:employment/id employment)
           (:location-memberships/employment-id location-membership))))))

(defn filter-by-start-and-end-dates [start-date end-date employments]
  (filter #(and (datetime/before? start-date (:employment/end %))
                (datetime/after? end-date (:employment/start %))) employments))

(defn find-employment-by-id [{:keys [db] :as api} employment-id]
  (if-let [employment (helpers/find-by-id db :employment employment-id)]
    (let [person (helpers/find-by-id db :people (:person-id employment))
          position (helpers/find-by-id db :positions (:position-id employment))]
      (response/success api (assoc employment
                                   :person person
                                   :position position)))
    (response/not-found api)))

(defn find-all-employments [{:keys [db] :as api} options]
  (let [start-date (:start-date options)
        end-date (:end-date options)
        location-id (:location-id options)]
    (-> (helpers/all-data-for-type db :employment {:namespace "employment"})
        (helpers/join (helpers/all-data-for-type db :people {:namespace "people"})
                      (fn [employment person]
                        (= (:employment/person-id employment)
                           (:people/id person))))
        (helpers/join (helpers/all-data-for-type db :positions {:namespace "positions"})
                      (fn [employment postion]
                        (= (:employment/position-id employment)
                           (:positions/id postion))))
        (as-> employments
          (if location-id
            (join-location-memberships api employments location-id)
            employments))
        (as-> employments
          (if start-date
            (filter
              (fn [employment]
                (if-let [end (:employment/end employment)]
                  (datetime/before? start-date end)
                  true))
              employments)
            employments))
        (as-> employments
          (if end-date
            (filter
              (fn [employment]
                (if-let [start (:employment/start employment)]
                  (datetime/after? end-date start)))
              employments)
            employments))
        (as-> employments
          (if (and location-id (or start-date end-date))
            (filter
              (fn [employment]
                (let [location-memberships (->> (helpers/all-data-for-type db :location-memberships)
                                               (filter
                                                 (fn [location-membership]
                                                   (= (:employment-id location-membership)
                                                      (:employment/id employment))))
                                               (sort (helpers/compare-fn :start :asc)))
                      location-memberships (loop [new-location-memberships []
                                                  i 2
                                                  current (first location-memberships)
                                                  next (nth location-memberships 1 nil)]
                                             (if next
                                               (recur
                                                 (conj new-location-memberships (assoc current :end (before (:start next) (days 1))))
                                                 (inc i)
                                                 next
                                                 (nth location-memberships i nil))
                                               (conj new-location-memberships current)))
                      ]
                  (some
                    (fn [location-membership]
                      (and
                        (= (:location-id location-membership) location-id)
                        (if end-date
                          (before? (:start location-membership) end-date)
                          true ; start is infinity, which is before the end date
                          )
                        (if (and (:end location-membership) start-date)
                          (after? (:end location-membership) start-date)
                          true ; end is infinity, which is after start date
                          )))
                    location-memberships)))
              employments)
            employments))
        (as-> employments
          (map
            (fn [row]
              (let [employment (helpers/select-keys-with-ns row "employment")
                    position (helpers/select-keys-with-ns row "positions")
                    person (helpers/select-keys-with-ns row "people")]
                (assoc employment :person person :position position)))
            employments))
        (as-> employments
          (if-let [sort-field (:sort options)]
            (let [direction (:direction options)
                  compare-fns (case sort-field
                                :full-name [(helpers/compare-fn #(-> % :person :first-name) direction)
                                            (helpers/compare-fn #(-> % :person :last-name) direction)]
                                :position [(helpers/compare-fn #(-> % :position :name) direction)]
                                :start [(helpers/compare-fn :start direction)]
                                :end [(helpers/compare-fn :end direction)]
                                [])]
              (sort (helpers/comparator-for-fns compare-fns) employments))
            employments))
        (as-> employments
          (response/success api employments)))))

(defn find-all-location-memberships-for-employment [{:keys [db] :as api} employment-id]
  (->> (helpers/find-where db :location-memberships #(= (:employment-id %) employment-id))
       (sort-by :id)
       (response/success api)))