(ns stockroom.admin.users.create-user
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.users.new-user :refer [respond-with-new-user-view]]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors
                                                   when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-create-user-request [{:keys [uid] :as params}]
  (if (and uid (not (string/blank? uid)))
    {}
    {:uid ["Please enter the Google uid of the user."]}))

(def duplicate-authentication-error "The uid you provided is already in use.")

(def authentication-api-error-mapping
  {:authentication/duplicate {:key :uid
                              :message duplicate-authentication-error}})

(defn translate-api-errors [errors]
  (api-errors-to-web-errors errors authentication-api-error-mapping))

(defn create-user [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-create-user-request params)]
    (if (seq errors)
      (respond-with-new-user-view {:request request
                                   :context context
                                   :errors errors
                                   :response-status 422})
      (when-status
        :success
        (fn [api user-id]
          (-> (response/redirect-after-post (urls/list-users-url context))
            (assoc-in [:flash :success] "Successfully created user.")
            (wring/set-user-api api)))
        :failure
        (fn [api errors]
          (let [errors (translate-api-errors errors)]
            (respond-with-new-user-view {:request request
                                         :context context
                                         :errors errors
                                         :response-status 422})))
        (api/create-user-with-authentication! api {:provider :google
                                                   :uid (:uid params)
                                                   :name (:name params)})))))
