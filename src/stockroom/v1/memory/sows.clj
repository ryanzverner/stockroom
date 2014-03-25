(ns stockroom.v1.memory.sows
  (:require [chee.datetime :as cd]
            [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.memory.project-sows :as project-sows]
            [stockroom.v1.response :as response]))

(defn create-sow! [{:keys [db] :as api} sow-data]
  (let [[db id] (helpers/insert db :sows sow-data)]
    (response/success (assoc api :db db) id)))

(defn update-sow! [{:keys [db] :as api} sow-id sow-data]
  (if-let [sow (helpers/find-by-id db :sows sow-id)]
    (let [finder #(= (:id %) sow-id)
          updater (fn [_] sow-data)
          db (helpers/update-where db :sows finder updater)]
      (response/success (assoc api :db db) nil))
    (response/not-found api)))

(defn find-sow-by-id [{:keys [db] :as api} sow-id]
  (if-let [sow (helpers/find-by-id db :sows sow-id)]
    (response/success api sow)
    (response/not-found api)))

(defn find-all-sows [{:keys [db] :as api} options]
  (let [sows (helpers/all-data-for-type db :sows {:namespace "sows"})
        sows (if-let [project-id (:project-id options)]
               (-> sows
                   (helpers/join
                     (helpers/all-data-for-type db :project-sows {:namespace "project-sows"})
                     (fn [sow project-sow]
                       (and
                         (= (:sows/id sow)
                            (:project-sows/sow-id project-sow))
                         (= project-id (:project-sows/project-id project-sow))))))
               sows)
        sows (map #(helpers/select-keys-with-ns % "sows") sows)]
    (response/success api sows)))

(defn delete-sow! [api sow-id]
  (let [{{:keys [db]} :api} (project-sows/delete-project-sows-for-sow! api sow-id)]
    (if-let [sow (helpers/find-by-id db :sows sow-id)]
      (let [finder #(= (:id %) sow-id)
            db (helpers/remove-where db :sows finder)]
        (response/success (assoc api :db db) nil))
      (response/not-found api))))