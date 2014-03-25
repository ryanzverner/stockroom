(ns stockroom.cli
  (:require [stockroom.config :as config])
  (:import (java.net URI)))

(defn arg-for-flag [args flag coerce default]
  (loop [[arg & more] args]
    (if arg
      (if (= arg flag)
        (coerce (first more))
        (recur (rest more)))
      default)))

(defn- session-params [uri]
  (case (.getScheme uri)
    "http" {:cookie-attrs {:secure false :http-only true}}
    "https" {:cookie-attrs {:secure true :http-only true}}))

(defn config-for-main-args [args defaults]
  (let [port      (arg-for-flag args "-p" #(Integer/parseInt %) (:port defaults))
        address   (arg-for-flag args "-a" identity "0.0.0.0")
        site-root (arg-for-flag args "-r" str (:site-root defaults))
        site-root (URI. (format site-root port))
        db-host   (arg-for-flag args "--db-host" identity (:db-host defaults))
        db-port   (arg-for-flag args "--db-port" identity (:db-port defaults))
        db-name   (arg-for-flag args "--db-name" identity (:db-name defaults))
        db-user   (arg-for-flag args "--db-user" identity (:db-user defaults))
        db-password (arg-for-flag args "--db-password" identity (:db-password defaults))
        db-spec {:test-connection-on-checkout true
                 :useLegacyDatetimeCode false
                 :serverTimezone "UTC"
                 :make-pool? true}
        db-spec (-> db-spec
                    (cond-> db-host (assoc :host db-host))
                    (cond-> db-port (assoc :port db-port))
                    (cond-> db-name (assoc :db db-name))
                    (cond-> db-user (assoc :user db-user))
                    (cond-> db-password (assoc :password db-password)))]
    {:conveyor (config/read-config "conveyor.clj")
     :db-spec db-spec
     :google-oauth2 (config/read-config "google-oauth2.clj")
     :jetty {:port port :host address}
     :session (session-params site-root)
     :site-root site-root}))
