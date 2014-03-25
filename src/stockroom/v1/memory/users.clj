(ns stockroom.v1.memory.users
  (:require [clojure.set :as set]
            [stockroom.v1.memory.helpers :as helpers]
            [stockroom.v1.response :as response]))

(defn find-user-by-id [api user-id]
  (if-let [user (helpers/find-by-id (:db api) :users user-id)]
    (response/success api user)
    (response/not-found api)))

(defn find-user-by-provider-and-uid [api provider uid]
  (let [provider (keyword (name provider))]
    (->> (helpers/all-data-for-type (:db api) :authentications)
         (filter #(and (= (:provider %) provider) (= (:uid %) uid)))
         (first)
         (:id)
         (find-user-by-id api))))

(defn create-user-with-authentication! [api {:keys [provider uid] :as user-data}]
  (if-let [user (:result (find-user-by-provider-and-uid api provider uid))]
    (response/failure api [response/duplicate-authentication])
    (let [[db user-id] (helpers/insert (:db api) :users {:name (:name user-data)})
          auth-data {:provider provider :uid uid :user-id user-id}
          [db auth-id] (helpers/insert db :authentications auth-data)]
      (response/success (assoc api :db db) user-id))))

(defn add-authentication-to-user! [api user-id {:keys [provider uid] :as auth-data}]
  (if-let [user (:result (find-user-by-id api user-id))]
    (if-let [user (:result (find-user-by-provider-and-uid api provider uid))]
      (response/failure api [response/duplicate-authentication])
      (let [auth {:provider provider :uid uid :user-id user-id}
            [db id] (helpers/insert (:db api) :authentications auth)]
        (response/success (assoc api :db db) auth)))
    (response/not-found api)))

(defn find-authentications-for-user [api user-id]
  (->> (helpers/all-data-for-type (:db api) :authentications)
       (filter #(= (:user-id %) user-id))
       (response/success api)))

(defn find-all-users [{:keys [db] :as api}]
  (response/success api (helpers/all-data-for-type db :users)))

(defn find-all-permissions-for-user [{:keys [db] :as api} user-id]
  (->> (helpers/find-where db :group-users #(= (:user-id %) user-id))
       (map :group-id)
       (set)
       ((fn [group-ids]
          (helpers/find-where db :group-permissions #(group-ids (:group-id %)))))
       (map :permission)
       (response/success api)))

(defn has-any-permission? [{:keys [db] :as api} user-id permissions]
  (let [needed-permissions (set permissions)
        user-perms (set (:result (find-all-permissions-for-user api user-id)))]
    (->> (set/intersection needed-permissions user-perms)
         (seq)
         (boolean)
         (response/success api))))
