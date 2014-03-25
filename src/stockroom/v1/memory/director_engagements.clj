(ns stockroom.v1.memory.director-engagements
  (:require [chee.datetime :as cd]
            [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]))

(defn create-director-engagement! [{:keys [db] :as api} director-engagement-data]
  (let [errors (validations/validate-director-engagement director-engagement-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (cond
        (= nil (helpers/find-by-id db :people (:person-id director-engagement-data)))
        (response/failure api [response/director-engagements-invalid-person-id])
        (= nil (helpers/find-by-id db :projects (:project-id director-engagement-data)))
        (response/failure api [response/director-engagements-invalid-project-id])
        :else
        (let [[db id] (helpers/insert db :director-engagements director-engagement-data)]
          (response/success (assoc api :db db) id))))))

(defn update-director-engagement! [{:keys [db] :as api} director-engagement-id director-engagement-data]
  (if-let [director-engagement (helpers/find-by-id db :director-engagements director-engagement-id)]
    (let [director-engagement-data (merge director-engagement director-engagement-data)
          errors (validations/validate-director-engagement director-engagement-data helpers/present? api)]
      (if (seq errors)
        (response/failure api errors)
        (cond
          (= nil (helpers/find-by-id db :people (:person-id director-engagement-data)))
          (response/failure api [response/director-engagements-invalid-person-id])
          (= nil (helpers/find-by-id db :projects (:project-id director-engagement-data)))
          (response/failure api [response/director-engagements-invalid-project-id])
          :else
          (let [finder #(= (:id %) director-engagement-id)
                updater (fn [_] director-engagement-data)
                db (helpers/update-where db :director-engagements finder updater)]
            (response/success (assoc api :db db) nil)))))
    (response/not-found api)))

(defn find-director-engagement-by-id [{:keys [db] :as api} director-engagement-id]
  (let [director-engagement (helpers/find-by-id db :director-engagements director-engagement-id)]
    (if director-engagement
      (response/success api director-engagement)
      (response/not-found api))))

(defn find-all-director-engagements-by-person-id [{:keys [db] :as api} director-id]
  (->> (helpers/find-where db :director-engagements #(= (:person-id %) director-id))
       (map #(assoc % :project (helpers/find-by-id db :projects (:project-id %))))
       (sort-by :start)
       (response/success api)))

(defn find-current-directors [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :director-engagements)
       (reduce (fn [acc director-engagement]
                 (if (and (helpers/compare-values >= (cd/now) (:start director-engagement))
                          (or (nil? (:end director-engagement))
                              (helpers/compare-values < (cd/now) (:end director-engagement))))
                   (conj acc (helpers/find-by-id db :people (:person-id director-engagement)))
                   acc))
               [])
       (distinct)
       (sort-by :id)
       (response/success api)))

(defn delete-director-engagements-for-project! [{:keys [db] :as api} project-id]
  (let [finder #(= (:project-id %) project-id)
        db (helpers/remove-where db :director-engagements finder)]
    (response/success (assoc api :db db) nil)))
