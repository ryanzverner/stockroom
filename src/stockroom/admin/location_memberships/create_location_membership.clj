(ns stockroom.admin.location-memberships.create-location-membership
  (:require [ring.util.response :as response]
            [stockroom.admin.location-memberships.form :as form]
            [stockroom.admin.location-memberships.new-location-membership :refer [respond-with-new-location-membership-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn get-form-params [params]
  (form/form-params->location-membership params))

(defn create-location-membership [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        form-params (get-form-params params)
        employment-id (get form-params :employment-id)
        location-id (get form-params :location-id)
        respond-with-errors (fn [errors]
                              (respond-with-new-location-membership-view {:context context
                                                                          :request request
                                                                          :errors errors
                                                                          :response-status 422}))
        errors (form/validate-location-membership-form params)]
    (if (seq errors)
      (respond-with-errors errors)
      (when-status
        :success
        (fn [api emp-id]
          (-> (response/redirect (urls/edit-employment-url context {:employment-id employment-id}))
            (assoc-in [:flash :success] "Successfully created Location Assignment.")
            (wring/set-user-api api)))
        :failure
        (fn [api errors]
          (-> errors
            form/translate-location-membership-api-errors-to-web-errors
            respond-with-errors))
        (api/create-location-membership! api
                                         employment-id
                                         location-id
                                         form-params)))))