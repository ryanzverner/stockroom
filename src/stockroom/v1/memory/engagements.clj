(ns stockroom.v1.memory.engagements
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]))

(defn assign-default-confidence-pct-if-missing [engagement-data]
  (if (nil? (:confidence-percentage engagement-data))
    (assoc engagement-data :confidence-percentage 100)
    engagement-data))

(defn create-engagement! [{:keys [db] :as api} engagement-data]
  (let [engagement-data (assign-default-confidence-pct-if-missing engagement-data)
        errors (validations/validate-engagement engagement-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (let [[db id] (helpers/insert db :engagements engagement-data)]
        (response/success (assoc api :db db) id)))))

(defn update-engagement! [{:keys [db] :as api} engagement-id engagement-data]
  (if-let [engagement (helpers/find-by-id db :engagements engagement-id)]
    (let [engagement-data (merge engagement engagement-data)
          errors (validations/validate-engagement engagement-data helpers/present? api)]
      (if (seq errors)
        (response/failure api errors)
        (let [finder #(= (:id %) engagement-id)
              updater (fn [_] engagement-data)
              db (helpers/update-where db :engagements finder updater)]
          (response/success (assoc api :db db) nil))))
    (response/not-found api)))

(defn find-engagement-by-id [{:keys [db] :as api} engagement-id]
  (if-let [engagement (helpers/find-by-id db :engagements engagement-id)]
    (response/success api engagement)
    (response/not-found api)))

(defn delete-engagement! [{:keys [db] :as api} engagement-id]
  (if-let [engagement (helpers/find-by-id db :engagements engagement-id)]
    (let [finder #(= (:id %) engagement-id)
          db (helpers/remove-where db :engagements finder)]
      (response/success (assoc api :db db) nil))
    (response/not-found api)))

(defn find-all-engagements [{:keys [db] :as api} {:keys [start end project-id]}]
  (let [date-filter (helpers/date-range-intersection-filter start end)
        project-id-filter (fn [engagement] (= (:project-id engagement) project-id))
        filter-if-project-id (fn [engagements] (if (nil? project-id)
                                                 engagements
                                                 (filter project-id-filter engagements)))
        get-person-id-from-employment-id #(:person-id (helpers/find-by-id db :employment %))]
    (->> (helpers/all-data-for-type db :engagements)
         (filter date-filter)
         (filter-if-project-id)
         (map #(assoc %
                      :project (helpers/find-by-id db :projects (:project-id %))
                      :person (helpers/find-by-id db :people (get-person-id-from-employment-id (:employment-id %)))))
         (response/success api))))
