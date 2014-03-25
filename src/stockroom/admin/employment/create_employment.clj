(ns stockroom.admin.employment.create-employment
  (:require [ring.util.response :as response]
            [stockroom.admin.employment.form :as form]
            [stockroom.admin.employment.new-employment :refer [respond-with-new-employment-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn create-employment [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        respond-with-errors (fn [errors]
                              (respond-with-new-employment-view {:context context
                                                                 :request request
                                                                 :errors errors
                                                                 :response-status 422}))
        errors (form/validate-employment-form params)]
    (if (seq errors)
      (respond-with-errors errors)
      (when-status
        :success
        (fn [api empoyment-id]
          (-> (response/redirect (urls/list-employments-url context))
            (assoc-in [:flash :success] "Successfully created employment.")
            (wring/set-user-api api)))
        :failure
        (fn [api errors]
          (-> errors
            form/translate-employment-api-errors-to-web-errors
            respond-with-errors))
        (api/create-employment! api (form/form-params->employment params))))))
