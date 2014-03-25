(ns stockroom.admin.skills.create-skill
  (:require [clojure.string :as string]
            [ring.util.response :as response]
            [stockroom.admin.skills.new-skill :refer [respond-with-new-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn validate-create-skill-request [{:keys [name]}]
  (if (or (nil? name) (string/blank? name))
    {:name ["Please enter a name."]}
    {}))

(defn create-skill [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-create-skill-request params)]
    (if (seq errors)
      (respond-with-new-skill-view {:request request
                                     :context context
                                     :errors errors
                                     :response-status 422})
      (when-status
        :success
        (fn [api skill-id]
            (-> (response/redirect-after-post (urls/list-skills-url context))
              (assoc-in [:flash :success] "Successfully created skill.")
              (wring/set-user-api api)))
        (api/create-skill! api {:name (:name params)})))))
