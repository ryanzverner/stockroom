(ns stockroom.admin.apprenticeships.list-apprenticeships
  (:require [hiccup.core :as hiccup]
            [clojure.string :refer [capitalize]]
            [stockroom.admin.apprenticeships.index-view :refer :all]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.v1.ring :as wring]
            [stockroom.v1.api :as api]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [month-day-year]]
            [ring.util.response :as response]))

(defn render-person-name [{:keys [first-name last-name]}]
  (format "%s %s" first-name last-name))

(defn render-mentorship [{:keys [person]}]
  (render-person-name person))

(defn render-mentorships [mentorships]
  (clojure.string/join ", " (map render-person-name 
                                 (map :person mentorships))))

(defn render-apprenticeship [{:keys [id person-id person skill-level start end mentorships] :as apprenticeship}]
  {:id id
   :person-id person-id
   :person-name (render-person-name person)
   :mentors (render-mentorships mentorships)
   :skill-level (capitalize skill-level)
   :start (month-day-year start)
   :end (month-day-year end)})

(defn build-view-data-for-apprenticeships-index-view [{:keys [apprenticeships context]}]
  {:new-apprenticeship-url (urls/new-apprenticeship-url context)
   :apprenticeships (map render-apprenticeship apprenticeships)})

(defn list-apprenticeships [context {:keys [params] :as request}]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api apprenticeships]
        (-> {:apprenticeships apprenticeships :context context}
            build-view-data-for-apprenticeships-index-view
            render-apprenticeships-index-view
            response/response))
      (api/find-all-apprenticeships api))))
