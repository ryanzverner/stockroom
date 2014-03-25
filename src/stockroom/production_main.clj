(ns stockroom.production-main
  (:require [conveyor.middleware :refer [wrap-pipeline-config]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.resource :refer [wrap-resource]]
            [stockroom.cli :refer [config-for-main-args]]
            [stockroom.root :refer [app]])
  (:gen-class))

(defn production-handler [{:keys [conveyor] :as config}]
  (-> (app config)
    (wrap-resource "public")
    wrap-file-info
    (wrap-pipeline-config (:production conveyor))))

(defn -main [& args]
  (let [config (config-for-main-args args {:port (Integer/parseInt (System/getenv "PORT"))
                                           :site-root (System/getenv "DOMAIN")
                                           :db-host (System/getenv "RDS_HOSTNAME")
                                           :db-port (Integer/parseInt (System/getenv "RDS_PORT"))
                                           :db-name (System/getenv "RDS_DB_NAME")
                                           :db-user (System/getenv "RDS_USERNAME")
                                           :db-password (System/getenv "RDS_PASSWORD")})]
    (println "Starting server in production mode...")
    (run-jetty (production-handler config) (:jetty config))))
