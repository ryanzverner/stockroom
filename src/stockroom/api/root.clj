(ns stockroom.api.root
  (:require [compojure.core :refer [routes]]
            [ring.util.response :as response]
            [stockroom.api.middleware.authentication :refer [wrap-authenticate-with-id-token]]
            [stockroom.api.middleware.format :refer [wrap-format]]
            [stockroom.api.util.request :as api-request]
            [stockroom.api.v1 :as v1]
            [stockroom.v1.ring :refer [wrap-authorized-user-api]]))

(defn not-found-handler [request]
  (response/not-found nil))

(defn app [next-handler]
  (-> (routes
        v1/handler
        not-found-handler)
    (wrap-authorized-user-api {:current-user-id-from-request api-request/current-user-id})
    wrap-authenticate-with-id-token
    (wrap-format next-handler)))
