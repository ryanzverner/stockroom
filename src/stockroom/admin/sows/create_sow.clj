(ns stockroom.admin.sows.create-sow
  (:require [ring.util.response :as response]
            [stockroom.admin.sows.form :as form]
            [stockroom.admin.sows.new-sow :refer [respond-with-new-sow-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn create-sow [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:client-id params)
        errors (form/validate-sow-form params)
        projects (:projects params)]
    (if (seq errors)
      (respond-with-new-sow-view {:context context
                                  :request request
                                  :api api
                                  :response-status 422
                                  :errors errors
                                  :params params
                                  :client-id client-id})

      (when-status
        :success
        (fn [api sow-id]
          (doseq [project-id (flatten (vector projects))]
            (api/create-project-sow! api {:sow-id sow-id :project-id project-id}))
          (-> (urls/show-client-url context {:client-id client-id})
            response/redirect-after-post
            (assoc-in [:flash :success] "Successfully created SOW.")
            (wring/set-user-api api)))
            (api/create-sow! api (form/form-params->sow params))))))
