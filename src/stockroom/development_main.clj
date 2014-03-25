(ns stockroom.development-main
  (:require [conveyor.middleware :refer [wrap-asset-pipeline]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [stockroom.cli :refer [config-for-main-args]]
            [stockroom.config :refer [read-config]]
            [stockroom.root :refer [app]]))

(defn development-handler [{:keys [conveyor] :as config}]
  (-> (#'app config)
    (wrap-asset-pipeline (:development conveyor))
    wrap-reload
    wrap-stacktrace))

(def db-development (:development (read-config "database.clj")))

(def main-defaults {:port 8080
                    :site-root "http://localhost:%s"
                    :db-host (:host db-development)
                    :db-port (:port db-development)
                    :db-name (:db db-development)
                    :db-user (:user db-development)
                    :db-password (:password db-development)})

(defn -main [& args]
  (let [config (config-for-main-args args main-defaults)]
    (prn config)
    (println "Starting server in development mode...")
    (run-jetty (development-handler config) (:jetty config))))
