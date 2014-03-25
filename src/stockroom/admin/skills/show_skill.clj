(ns stockroom.admin.skills.show-skill
  (:require [ring.util.response :as response]
            [stockroom.admin.skills.show-view :refer [render-show-skill-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-show-skill-view [{:keys [context skill]}]
  {:skill-name (:name skill)})

(defn show-skill [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        skill-id (:id params)]
    (when-status
      :success
      (fn [api skill]
        (-> {:context context
             :skill skill}
          build-view-data-for-show-skill-view
          render-show-skill-view
          response/response
          (wring/set-user-api api)))
      (api/find-skill-by-id api skill-id))))
