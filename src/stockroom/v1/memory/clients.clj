(ns stockroom.v1.memory.clients
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.memory.projects :as projects]
            [stockroom.v1.response :as response]
            [clojure.tools.trace :as t]))

(defn create-client! [{:keys [db] :as api} options]
  (let [[db id] (helpers/insert db :clients options)]
    (response/success (assoc api :db db) id)))

(defn update-client! [{:keys [db] :as api} client-id options]
  (let [finder #(= (:id %) client-id)
        updater (fn [client] options)
        db (helpers/update-where db :clients finder updater)]
    (response/success (assoc api :db db) nil)))

(defn find-client-by-id [{:keys [db] :as api} client-id]
  (if-let [client (helpers/find-by-id db :clients client-id)]
    (response/success api client)
    (response/not-found api)))

(defn find-all-clients [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :clients)
       (response/success api)))

(defn delete-client! [api client-id]
  (let [projects (:result (projects/find-all-projects-for-client api client-id))
        api (reduce (fn [api project] 
                      (:api (projects/delete-project! api (:id project))))
                    api
                    projects)]
    (let [{:keys [api] client :result} (find-client-by-id api client-id)]
      (if client
        (let [finder #(= (:id %) client-id)
              db (helpers/remove-where (:db api) :clients finder)]
          (response/success (assoc api :db db) nil))
        (response/not-found api)))))
