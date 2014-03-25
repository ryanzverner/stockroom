(ns stockroom.admin.employment.new-employment
  (:require [stockroom.admin.employment.form :refer :all]
            [stockroom.admin.employment.new-view :refer [render-new-employment-view]]
            [stockroom.admin.url-helper :as urls]))

(defn build-view-data-for-new-employment-view [{:keys [context] :as options}]
  (merge {:create-employment-url (urls/create-employment-url context)}
         (build-view-data-for-employment-form options)))

(defn respond-with-new-employment-view [{:keys [context] :as options}]
  (respond-with-employment-form
    (fn [form-view-data form-body]
      (->{:context context}
        build-view-data-for-new-employment-view
        (render-new-employment-view form-body)))
    options))

(defn new-employment [context request]
  (respond-with-new-employment-view {:context context
                                     :request request
                                     :errors {}
                                     :response-status 200}))
