(ns stockroom.admin.apprenticeships.create-apprenticeship
  (:require [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.admin.util.view-helper :refer :all]
            [stockroom.v1.ring :as wring]))

(defn params->mentorship [apprentice-id {:keys [mentor-id mentorship-start-date mentorship-end-date] :as params}]
  {:person-id mentor-id
   :apprentice-id apprentice-id
   :start (date-from-input mentorship-start-date)
   :end (date-from-input mentorship-end-date)})

(defn params->apprenticeship [{:keys [apprentice-id skill-level apprenticeship-start-date apprenticeship-end-date] :as params}]
  {:person-id apprentice-id
   :skill-level skill-level
   :start (date-from-input apprenticeship-start-date)
   :end (date-from-input apprenticeship-end-date)
   :mentorships [(params->mentorship apprentice-id params)]})

(defn create-apprenticeship [context {:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api apprenticeship-id]
        (-> (response/redirect (urls/list-apprenticeships-url context))
            (wring/set-user-api api)))

      :failure
      (fn [api errors]
        (-> (response/redirect (urls/list-apprenticeships-url context))
            (wring/set-user-api api)))

      (api/create-apprenticeship! api (params->apprenticeship params)))))
