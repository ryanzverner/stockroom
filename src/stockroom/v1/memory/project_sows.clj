(ns stockroom.v1.memory.project-sows
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]))

(defn create-project-sow! [{:keys [db] :as api} project-sow-data]
  (let [errors (validations/validate-project-sow project-sow-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (let [[db id] (helpers/insert db :project-sows project-sow-data)]
        (response/success (assoc api :db db) id)))))

(defn update-project-sow! [{:keys [db] :as api} project-sow-id project-sow-data]
  (if-let [project-sow (helpers/find-by-id db :project-sows project-sow-id)]
    (let [project-sow-data (merge project-sow project-sow-data)
          errors (validations/validate-project-sow project-sow-data helpers/present? api)]
      (if (seq errors)
        (response/failure api errors)
        (let [finder #(= (:id %) project-sow-id)
              updater (fn [_] project-sow-data)
              db (helpers/update-where db :project-sows finder updater)]
          (response/success (assoc api :db db) nil))))
    (response/not-found api)))

(defn find-project-sow-by-id [{:keys [db] :as api} project-sow-id]
  (if-let [project-sow (helpers/find-by-id db :project-sows project-sow-id)]
    (response/success api project-sow)
    (response/not-found api)))

(defn find-all-project-sows [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :project-sows)
       (response/success api)))

(defn delete-project-sow! [{:keys [db] :as api} project-sow-id]
  (if-let [project-sow (helpers/find-by-id db :project-sows project-sow-id)]
    (let [finder #(= (:id %) project-sow-id)
          db (helpers/remove-where db :project-sows finder)]
      (response/success (assoc api :db db) nil))
    (response/not-found api)))

(defn delete-project-sows-for-sow! [{:keys [db] :as api} sow-id]
  (let [finder #(= (:sow-id %) sow-id)
        db (helpers/remove-where db :project-sows finder)]
    (response/success (assoc api :db db) nil)))