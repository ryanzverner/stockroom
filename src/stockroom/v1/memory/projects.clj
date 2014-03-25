(ns stockroom.v1.memory.projects
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.memory.project-skills :as project-skills]
            [stockroom.v1.memory.director-engagements :as director-engagements]
            [stockroom.v1.response :as response]))

(defn create-project! [{:keys [db] :as api} {:keys [client-id] :as options}]
  (let [[client-id-option-present? client] (if client-id
                                             [true (helpers/find-by-id db :clients client-id)]
                                             [false nil])]
    (if (or (and client-id-option-present? client)
            (not client-id-option-present?))
      (let [[db id] (helpers/insert db :projects options)]
        (response/success (assoc api :db db) id))
      (response/not-found api))))

(defn update-project! [{:keys [db] :as api} project-id options]
  (let [finder #(= (:id %) project-id)
        updater (fn [_] (dissoc options :client-id))
        db (helpers/update-where db :projects finder updater)]
    (response/success (assoc api :db db) nil)))

(defn find-project-by-id [{:keys [db] :as api} project-id]
  (if-let [project (helpers/find-by-id db :projects project-id)]
    (response/success api project)
    (response/not-found api)))

(defn find-all-projects [{:keys [db] :as api} options]
  (let [sort-field (:sort options)
        sort-direction (:direction options)
        sort-fn (if (and sort-field sort-direction)
                  (let [sort-fn #(sort-by sort-field %)]
                    (if (= :desc sort-direction)
                      #(reverse (sort-fn %))
                      sort-fn))
                  (fn [results] results))
        projects (helpers/all-data-for-type db :projects {:namespace "projects"})
        projects (if-let [sow-id (:sow-id options)]
                    (-> projects
                        (helpers/join
                          (helpers/all-data-for-type db :project-sows {:namespace "project-sows"})
                          (fn [project project-sow]
                            (and
                              (= (:projects/id project)
                                 (:project-sows/project-id project-sow))
                              (= sow-id (:project-sows/sow-id project-sow))))))
                    projects)
        projects (map #(helpers/select-keys-with-ns % "projects") projects)]
    (->> projects
         sort-fn
         (response/success api))))

(defn find-all-projects-for-client [{:keys [db] :as api} client-id]
  (->> (helpers/find-where db :projects #(= (:client-id %) client-id))
       (response/success api)))

(defn delete-project! [{:keys [db] :as api} project-id]
    (if-let [project (helpers/find-by-id db :projects project-id)]
      (let [{{:keys [db] :as api} :api} (project-skills/delete-project-skills-for-project! api project-id)
            {{:keys [db] :as api} :api} (director-engagements/delete-director-engagements-for-project! api project-id)
            finder #(= (:id %) project-id)
            db (helpers/remove-where db :projects finder)]
        (response/success (assoc api :db db) nil))
      (response/not-found api)))

(defn delete-projects-for-client! [{:keys [db] :as api} client-id]
  (let [finder #(= (:client-id %) client-id)
        db (helpers/remove-where db :projects finder)]
    (response/success (assoc api :db db) nil)))