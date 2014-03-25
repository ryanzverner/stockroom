(ns stockroom.v1.memory.project-skills
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.validations :as validations]))

(defn create-project-skill! [{:keys [db] :as api} project-skill-data]
  (let [errors (validations/validate-project-skill project-skill-data helpers/present? api)]
    (if (seq errors)
      (response/failure api errors)
      (let [[db id] (helpers/insert db :project-skills project-skill-data)]
        (response/success (assoc api :db db) id)))))

(defn find-project-skill-by-id [{:keys [db] :as api} project-skill-id]
  (if-let [project-skill (helpers/find-by-id db :project-skills project-skill-id)]
    (response/success api project-skill)
    (response/not-found api)))

(defn delete-project-skills-for-project! [{:keys [db] :as api} project-id]
  (let [finder #(= (:project-id %) project-id)
        db (helpers/remove-where db :project-skills finder)]
    (response/success (assoc api :db db) nil)))