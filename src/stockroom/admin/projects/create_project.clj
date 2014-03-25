(ns stockroom.admin.projects.create-project
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.projects.new-project :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-create-project-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn create-project [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        client-id (:client-id params)
        errors (validate-create-project-request params)]
    (if (seq errors)
      (respond-with-new-project-view {:context context
                                      :response-status 422
                                      :errors errors
                                      :params params
                                      :request request
                                      :client-id client-id})
      (when-status
        :success
        (fn [api project-id]
          (-> (urls/show-client-url context {:client-id client-id})
            response/redirect-after-post
            (wring/set-user-api api)
            (assoc-in [:flash :success] "Successfully created project.")))
        (api/create-project! api {:name (:name params)
                                  :client-id client-id})))))
