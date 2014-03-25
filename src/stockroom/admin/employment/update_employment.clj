(ns stockroom.admin.employment.update-employment
  (:require [ring.util.response :as response]
            [stockroom.admin.employment.edit-employment :refer [respond-with-edit-employment-view]]
            [stockroom.admin.employment.form :as form]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn respond-with-errors [errors context request]
  (respond-with-edit-employment-view {:context context
                                      :request request
                                      :errors errors
                                      :response-status 422}))

(defn update-employment [context request]
  (let [api (wring/user-api request)
        params (:params request)
        employment-id (:employment-id params)
        errors (form/validate-employment-form params)]
    (if (seq errors)
      (respond-with-errors errors context request)
      (when-status
        :success
        (fn [api _]
          (-> (response/redirect (urls/list-employments-url context))
            (assoc-in [:flash :success] "Successfully updated employment.")
            (wring/set-user-api api)))
        :failure
        (fn [api errors]
          (-> errors
            form/translate-employment-api-errors-to-web-errors
            (respond-with-errors context request)))
        (api/update-employment! api employment-id
                                (form/form-params->employment params))))))
