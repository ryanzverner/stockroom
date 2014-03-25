(ns stockroom.admin.root
  (:require [compojure.core :refer [GET routes]]
            [hiccup.def :refer [defhtml]]
            [stockroom.admin.auth.routes :as auth]
            [stockroom.admin.clients.routes :as clients]
            [stockroom.admin.skills.routes :as skills]
            [stockroom.admin.context :refer [admin-context]]
            [stockroom.admin.employment.routes :as employment]
            [stockroom.admin.locations.routes :as locations]
            [stockroom.admin.groups.routes :as groups]
            [stockroom.admin.middleware.auth :refer [wrap-load-logged-in-user
                                                     wrap-require-logged-in-user]]
            [stockroom.admin.middleware.layout :refer [wrap-layout]]
            [stockroom.admin.people.routes :as people]
            [stockroom.admin.projects.routes :as projects]
            [stockroom.admin.sows.routes :as sows]
            [stockroom.admin.users.routes :as users]
            [stockroom.admin.apprenticeships.routes :as apprenticeships]
            [stockroom.admin.util.request :as admin-request]
            [stockroom.admin.util.response :refer [not-found-response]]
            [stockroom.middleware.accept :refer [wrap-accept]]
            [stockroom.v1.ring :as wring]))

(defn not-found-handler [request]
  (not-found-response (wring/user-api request)))

(defhtml render-root-view []
  [:p "Welcome to stockroom Admin!"])

(defn- admin-routes [ctx]
  (routes
    (auth/handler ctx)
    (-> (routes
          (GET "/" request (render-root-view))
          (users/handler ctx)
          (groups/handler ctx)
          (clients/handler ctx)
          (skills/handler ctx)
          (projects/handler ctx)
          (sows/handler ctx)
          (employment/handler ctx)
          (locations/handler ctx)
          (people/handler ctx)
          (apprenticeships/handler ctx))
      (wrap-require-logged-in-user ctx))
    not-found-handler))

(defn handler [next-handler ctx]
  (-> (admin-routes ctx)
    (wrap-layout ctx)
    (wring/wrap-authorized-user-api {:current-user-id-from-request admin-request/current-user-id})
    (wrap-load-logged-in-user ctx)
    (wrap-accept next-handler "text" "html")))

(defn app [next-handler site-root google-oauth2-config]
  (handler
    next-handler
    (admin-context {:host-uri site-root
                    :url-prefix "/"
                    :google-oauth2-client-id (:client-id google-oauth2-config)
                    :google-oauth2-client-secret (:client-secret google-oauth2-config)})))
