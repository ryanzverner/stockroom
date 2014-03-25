(ns stockroom.admin.people.create-person
  (:require [ring.util.response :as response]
            [stockroom.admin.people.form :as form]
            [stockroom.admin.people.new-person :refer [respond-with-new-person-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn create-person [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (form/validate-person-form params)]
    (if (seq errors)
      (respond-with-new-person-view {:context context
                                     :api api
                                     :response-status 422
                                     :errors errors
                                     :params params})
      (when-status
        :success
        (fn [api person]
          (-> (urls/list-people-url context)
            response/redirect
            (assoc-in [:flash :success] "Successfuly created person.")
            (wring/set-user-api api)))
        (api/create-person! api (form/form-params->person params))))))
