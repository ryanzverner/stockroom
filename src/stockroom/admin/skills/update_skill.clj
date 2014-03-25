(ns stockroom.admin.skills.update-skill
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.skills.edit-skill :refer [respond-with-edit-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-update-skill-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn update-skill [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-update-skill-request params)
        skill-id (-> request :params :id)]
    (if (seq errors)
      (respond-with-edit-skill-view {:context context
                                      :request request
                                      :errors errors
                                      :response-status 422
                                      :skill-id skill-id})
      (when-status
        :success
        (fn [api _]
          (-> (response/redirect-after-post (urls/list-skills-url context))
            (assoc-in [:flash :success] "Successfully updated skill.")
            (wring/set-user-api api)))
        (api/update-skill! api skill-id {:name (:name params)})))))
