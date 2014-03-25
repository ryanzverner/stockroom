(ns stockroom.admin.sows.update-sow
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.sows.edit-sow :refer :all]
            [stockroom.admin.sows.form :as form]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn update-sow [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        sow-id (:sow-id params)
        client-id (:client-id params)
        projects (:projects params)
        errors (form/validate-sow-form params)]
    (if (seq errors)
      (respond-with-edit-sow-view {:context context
                                   :sow-id sow-id
                                   :client-id client-id
                                   :response-status 422
                                   :errors errors
                                   :request request})
    (when-status
      :success
      (fn [api _]
        (api/delete-project-sows-for-sow! api sow-id)
        (doseq [project-id (flatten (vector projects))]
          (api/create-project-sow! api {:sow-id sow-id :project-id project-id}))
        (-> (urls/show-client-url context {:client-id client-id})
          response/redirect-after-post
          (wring/set-user-api api)
          (assoc-in [:flash :success] "Successfully updated SOW.")))
      (api/update-sow! api sow-id (form/form-params->sow params))))))