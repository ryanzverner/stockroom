(ns stockroom.api.v1.directors-spec
  (:require [speclj.core :refer :all]
            [stockroom.api.spec-helper :refer :all]
            [stockroom.api.v1 :refer [handler]]
            [stockroom.util.time :refer [days-ago-at-midnight days-from-now-at-midnight]]
            [stockroom.api.v1.format :refer [format-date-for-web]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(def twelve-days-ago (days-ago-at-midnight 12))
(def ten-days-ago (days-ago-at-midnight 10))
(def five-days-ago (days-ago-at-midnight 5))
(def five-days-from-now (days-from-now-at-midnight 5))

(defn test-director-engagement [api options]
  (let [{api :api project-id :result} (api/create-project! api (merge {:name "test"} (:project options)))
        {api :api director-engagement-id :result} (api/create-director-engagement! api (merge {:person-id (:person-id options)
                                                                                               :project-id project-id
                                                                                               :start ten-days-ago}
                                                                                              options))]
    [api director-engagement-id]))

(describe "/v1/directors"

  (it "finds all current directors"
    (let [{api :api person-id-1 :result} (api/create-person! (test-stockroom-api) {:first-name "Kevin"})
          [api _] (test-director-engagement api
                                            {:end five-days-from-now
                                             :person-id person-id-1})
          {api :api person-id-2 :result} (api/create-person! api {:first-name "Kevin"})
          [api _] (test-director-engagement api
                                            {:end five-days-ago
                                             :person-id person-id-2})
          request (-> (request :get "/v1/directors/current")
                      (wring/set-user-api api))
          {{directors :directors} :body :as response} (handler request)]
      (should= 200 (:status response))
      (should= 1 (count directors))
      (should= "Kevin" (:first-name (first directors)))))

  (it "finds all director-engagements for the given director"
    (let [{api :api person-id :result} (api/create-person! (test-stockroom-api) {:first-name "Kevin"})
          [api _] (test-director-engagement api
                                            {:start twelve-days-ago
                                             :end five-days-from-now
                                             :person-id person-id
                                             :project {:name "Vision"}})
          [api _] (test-director-engagement api
                                            {:start ten-days-ago
                                             :end five-days-ago
                                             :person-id person-id
                                             :project {:name "stockroom"}})
          request (-> (request :get (format "/v1/directors/%s/director-engagements" person-id))
                      (wring/set-user-api api))
          {{director-engagements :director-engagements} :body :as response} (handler request)
          director-engagement-1 (first director-engagements)
          director-engagement-2 (second director-engagements)]
      (should= 200 (:status response))
      (should= 2 (count director-engagements))
      (should= "Vision" (:name (:project director-engagement-1)))
      (should= (format-date-for-web twelve-days-ago) (:start director-engagement-1))
      (should= (format-date-for-web five-days-from-now) (:end director-engagement-1))
      (should= "stockroom" (:name (:project director-engagement-2)))
      (should= (format-date-for-web ten-days-ago) (:start director-engagement-2))
      (should= (format-date-for-web five-days-ago) (:end director-engagement-2))))
  )

