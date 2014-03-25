(ns stockroom.admin.middleware.auth
  (:require [stockroom.admin.auth.login-view :refer [render-user-not-found]]
            [stockroom.admin.util.request :as admin-request]
            [stockroom.admin.util.response :as admin-response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn find-user-for-provider-and-uid [api provider uid]
  (when-status
    :success
    (fn [api user] user)
    :not-found
    (fn [api] nil)
    (api/find-user-by-provider-and-uid api provider uid)))

(defn wrap-load-logged-in-user [handler context]
  (fn [request]
    (let [api (wring/service-api request)
          {:keys [uid provider]} (admin-request/current-uid-and-provider request)]
      (if (and uid provider)
        (if-let [user (find-user-for-provider-and-uid api provider uid)]
          (handler (admin-request/set-current-user request user))
          (handler request))
        (handler request)))))

(defn wrap-require-logged-in-user [handler context]
  (fn [request]
    (if (admin-request/current-user request)
      (handler request)
      (let [{:keys [uid provider]} (admin-request/current-uid-and-provider request)]
        (if (and uid provider)
          {:status 401 :body (render-user-not-found provider uid)}
          (admin-response/redirect-to-login-url context request))))))
