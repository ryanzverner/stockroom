(ns stockroom.v1.memory.groups
  (:require [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]
            [stockroom.v1.memory.users :as users]))

(defn create-permissions-group! [{:keys [db] :as api} group-data]
  (if-let [group (helpers/find-group-by-name db (:name group-data))]
    (response/failure api [response/duplicate-group-name])
    (let [[db id] (helpers/insert db :groups group-data)]
      (response/success (assoc api :db db) id))))

(defn find-all-permission-groups [{:keys [db] :as api}]
  (->> (helpers/all-data-for-type db :groups)
       (response/success api)))

(defn find-permissions-for-group [{:keys [db] :as api} group-id]
  (->> (helpers/all-data-for-type db :group-permissions)
       (filter #(= (:group-id %) group-id))
       (map :permission)
       (response/success api)))

(defn find-permission-group-by-id [{:keys [db] :as api} group-id]
  (if-let [group (helpers/find-by-id db :groups group-id)]
    (response/success api group)
    (response/not-found api)))

(defn add-permission-to-group! [{:keys [db] :as api} {:keys [group-id permission] :as options}]
  (if-let [group (:result (find-permission-group-by-id api group-id))]
    (let [permissions-for-group (:result (find-permissions-for-group api group-id))]
      (if (some #(= % permission) permissions-for-group)
        (response/success api nil)
        (let [[db id] (helpers/insert db :group-permissions options)]
          (response/success (assoc api :db db) nil))))
    (response/not-found api)))

(defn remove-permission-from-group! [{:keys [db] :as api} {:keys [group-id permission]}]
  (let [finder #(and (= (:group-id %) group-id) (= (:permission %) permission))]
    (if (seq (helpers/find-where db :group-permissions finder))
      (let [db (helpers/remove-where db :group-permissions finder)]
        (response/success (assoc api :db db) nil))
      (response/success api nil))))

(defn find-all-users-in-group [{:keys [db] :as api} group-id]
  (->> (helpers/find-where db :group-users #(= (:group-id %) group-id))
       (map :user-id)
       (map #(users/find-user-by-id api %))
       (map :result)
       (response/success api)))

(defn add-user-to-group! [{:keys [db] :as api} {:keys [user-id group-id] :as options}]
  (if-let [group (:result (find-permission-group-by-id api group-id))]
    (if-let [user (:result (users/find-user-by-id api user-id))]
      (let [users-in-group (:result (find-all-users-in-group api group-id))]
        (if (some #(= (:id %) user-id) users-in-group)
          (response/success api nil)
          (let [[db id] (helpers/insert db :group-users options)]
            (response/success (assoc api :db db) nil))))
      (response/not-found api))
    (response/not-found api)))

(defn remove-user-from-group! [{:keys [db] :as api} {:keys [user-id group-id] :as options}]
  (let [finder #(and (= (:group-id %) group-id) (= (:user-id %) user-id))]
    (if (seq (helpers/find-where db :group-users finder))
      (let [db (helpers/remove-where db :group-users finder)]
        (response/success (assoc api :db db) nil))
      (response/success api nil))))

(defn find-all-groups-for-user [{:keys [db] :as api} user-id]
  (->> (helpers/find-where db :group-users #(= (:user-id %) user-id))
       (map :group-id)
       (map #(find-permission-group-by-id api %))
       (map :result)
       (response/success api)))
