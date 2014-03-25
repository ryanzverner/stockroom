(ns stockroom.admin.people.edit-person
  (:require [ring.util.response :as response]
            [stockroom.admin.people.edit-view :refer [render-edit-person-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn person->form-params [person params]
  {:first-name (or (:first-name params) (:first-name person))
   :last-name (or (:last-name params) (:last-name person))
   :email (or (:email params) (:email person))})

(defn build-view-data-for-edit-view [{:keys [context params person errors]}]
  {:update-person-url (urls/update-person-url context {:person-id (:id person)})
   :params (person->form-params person params)
   :errors errors})

(defn respond-with-edit-person-view [{:keys [api person-id params errors context response-status]}]
  (when-status
    :success
    (fn [api person]
      (-> {:context context :params params :errors errors :person person}
        build-view-data-for-edit-view
        render-edit-person-view
        response/response
        (response/status response-status)))
    (api/find-person-by-id api person-id)))

(defn edit-person [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        person-id (:person-id params)]
    (respond-with-edit-person-view {:context context
                                    :api api
                                    :person-id person-id
                                    :response-status 200
                                    :errors {}
                                    :params params})))
