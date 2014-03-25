(ns stockroom.v1.memory.skills
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]))

(defn create-skill! [{:keys [db] :as api} options]
  (let [[db id] (helpers/insert db :skills options)]
    (response/success (assoc api :db db) id)))

(defn update-skill! [{:keys [db] :as api} skill-id options]
  (let [finder #(= (:id %) skill-id)
        updater (fn [skill] options)
        db (helpers/update-where db :skills finder updater)]
    (response/success (assoc api :db db) nil)))

(defn find-skill-by-id [{:keys [db] :as api} skill-id]
  (if-let [skill (helpers/find-by-id db :skills skill-id)]
    (response/success api skill)
    (response/not-found api)))

(defn find-all-skills [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :skills)
       (response/success api)))

(defn find-all-skills-for-project [{:keys [db] :as api} project-id]
  (->> (helpers/find-where db :project-skills #(= (:project-id %) project-id))
       (map :skill-id)
       (set)
       ((fn [skill-ids]
          (helpers/find-where db :skills #(skill-ids (:skill-ids %)))))
       (map :skill)
       (response/success api)))
