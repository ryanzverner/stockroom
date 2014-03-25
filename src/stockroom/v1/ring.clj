(ns stockroom.v1.ring
  (:require [stockroom.v1.authorized-api :refer [wrap-with-authorized-api]]
            [stockroom.v1.mysql-api :refer [mysql-api with-db]]))

(def user-api-request-key :stockroom/user-api)
(def service-api-request-key :stockroom/service-api)

(defn set-user-api [request api]
  (assoc request user-api-request-key api))

(defn user-api [request]
  (user-api-request-key request))

(defn set-service-api [request api]
  (assoc request service-api-request-key api))

(defn service-api [request]
  (service-api-request-key request))

(defn wrap-api [handler api]
  (fn [request]
    (-> request
      (set-user-api api)
      (set-service-api api)
      handler)))

(defn wrap-mysql-api [handler db-spec]
  (wrap-api handler (mysql-api db-spec)))

(defn wrap-authorized-user-api [handler {:keys [current-user-id-from-request]}]
  (fn [request]
    (let [user-api        (user-api request)
          current-user-id (current-user-id-from-request request)
          authorized-api  (wrap-with-authorized-api user-api current-user-id)]
      (-> request
        (set-user-api authorized-api)
        handler))))
