(ns stockroom.admin.people.update-person
  (:require [ring.util.response :as response]
            [stockroom.admin.people.edit-person :refer [respond-with-edit-person-view]]
            [stockroom.admin.people.form :as form]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn update-person [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        person-id (:person-id params)
        errors (form/validate-person-form params)]
    (if (seq errors)
      (respond-with-edit-person-view {:context context
                                      :api api
                                      :person-id person-id
                                      :response-status 422
                                      :errors errors
                                      :params params})
      (when-status
        :success
        (fn [api _]
          (-> (urls/list-people-url context)
            response/redirect
            (assoc-in [:flash :success] "Successfuly updated person.")
            (wring/set-user-api api)))
        (api/update-person! api person-id (form/form-params->person params))))))
