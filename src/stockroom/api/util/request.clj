(ns stockroom.api.util.request)

(defn set-current-user [request user]
  (assoc request :api/current-user user))

(defn current-user [request]
  (:api/current-user request))

(defn current-user-id [request]
  (-> request
    current-user
    :id))
