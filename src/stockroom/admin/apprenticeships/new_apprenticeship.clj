(ns stockroom.admin.apprenticeships.new-apprenticeship
  (:require [ring.util.response :as response]
            [stockroom.admin.apprenticeships.new-view :refer :all]
            [stockroom.admin.form-helper :refer :all]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors when-status]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-new-apprenticeship-view [{:keys [context params errors people] :as options}]
  {:create-apprenticeship-url (urls/create-apprenticeship-url context)
   :params params
   :errors errors
   :person-options (person-select-options people)})

(defn new-apprenticeship [context {:keys [params errors] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api people]
        (-> {:context context
             :params params
             :people people}
            (build-view-data-for-new-apprenticeship-view)
            (render-new-apprenticeship-view)
            (response/response)))
      (api/find-all-people api))))
