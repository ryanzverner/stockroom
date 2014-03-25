(ns stockroom.admin.clients.create-client
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.clients.new-client :refer [respond-with-new-client-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-create-client-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn create-client [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-create-client-request params)]
    (if (seq errors)
      (respond-with-new-client-view {:request request
                                     :context context
                                     :errors errors
                                     :response-status 422})
      (when-status
        :success
        (fn [api client-id]
            (-> (response/redirect-after-post (urls/list-clients-url context))
              (assoc-in [:flash :success] "Successfully created client.")
              (wring/set-user-api api)))
        (api/create-client! api {:name (:name params)})))))
