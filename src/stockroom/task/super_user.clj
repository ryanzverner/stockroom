(ns stockroom.task.super-user
  (:require [stockroom.v1.api :as api]
            [stockroom.v1.permissions :as permissions]
            [stockroom.v1.mysql-api :refer [mysql-api with-db]]
            [stockroom.config  :as config]))

(defn add-permissions-to-group [api group-id permissions]
  (loop [permissions permissions api api]
    (if (seq permissions)
      (let [options {:group-id group-id :permission (first permissions)}
            {api :api} (api/add-permission-to-group! api options)]
        (recur (next permissions) api))
      api)))

(defn -main [& args]
  (let [uid (first args)
        db-spec (:development (config/read-config "database.clj"))
        api (mysql-api db-spec)]
    (with-db
      api

      ; create a user with uid
      ; create a group with all permissions
      ; add user to new group
      ; add all permissions to group
      (let [{api :api user-id :result} (api/create-user-with-authentication! api {:provider :google :uid uid :name "Test User"})
            {api :api group-id :result} (api/create-permissions-group! api {:name "Super Users"})
            {api :api} (api/add-user-to-group! api {:group-id group-id :user-id user-id})]
        (add-permissions-to-group api group-id permissions/all-permissions)))))
