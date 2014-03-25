(ns stockroom.admin.locations.create-location
  (:require [ring.util.response :as response]
            [stockroom.admin.locations.form :as form]
            [stockroom.admin.locations.new-location :refer [respond-with-new-location-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn create-location [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (form/validate-location-form params)]
    (if (seq errors)
      (respond-with-new-location-view {:context context
                                       :api api
                                       :response-status 422
                                       :errors errors
                                       :params params})
      (when-status
        :success
        (fn [api location]
          (-> (urls/list-locations-url context)
            response/redirect
            (assoc-in [:flash :success] "Successfully created location.")
            (wring/set-user-api api)))
        (api/create-location! api (form/form-params->location params))))))
