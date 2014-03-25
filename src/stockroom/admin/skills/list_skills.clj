(ns stockroom.admin.skills.list-skills
  (:require [ring.util.response :as response]
            [stockroom.admin.skills.index-view :refer [render-skills-index-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-skills-index-view [{:keys [skills context]}]
  {:new-skill-url (urls/new-skill-url context)
   :skills (map
              (fn [c]
                {:name (:name c)
                 :show-url (urls/show-skill-url context {:skill-id (:id c)})
                 :edit-url (urls/edit-skill-url context {:skill-id (:id c)})})
              skills)})

(defn list-skills [context request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api skills]
        (-> {:skills skills :context context}
          build-view-data-for-skills-index-view
          render-skills-index-view
          response/response))
      (api/find-all-skills api))))
