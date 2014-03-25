(ns stockroom.root
  (:require [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.head :refer [wrap-head]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [stockroom.admin.root :as admin]
            [stockroom.api.root :as api]
            [stockroom.v1.ring :refer [wrap-mysql-api]]))

(defn unacceptable [request]
  {:status 406 :body ""})

(defn app [config]
  (-> unacceptable
    (admin/app (:site-root config) (:google-oauth2 config))
    api/app
    (wrap-mysql-api (:db-spec config))
    wrap-keyword-params
    wrap-params
    wrap-flash
    (wrap-session (:session config))
    wrap-head))
