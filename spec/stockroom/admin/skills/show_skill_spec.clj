(ns stockroom.admin.skills.show-skill-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.skills.show-skill :refer :all]
            [stockroom.admin.spec-helper :refer [test-admin-context]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [date-for-input]]
            [stockroom.util.time :refer [days-from-now-at-midnight]]))

(def two-days-from-now (days-from-now-at-midnight 2))
(def five-days-from-now (days-from-now-at-midnight 5))

(describe "stockroom.admin.skills.show-skill"

  (with ctx (test-admin-context))

  (it "builds view data for show skill view"
    (let [skill {:id 10 :name "test skill"}]
      (should= {:skill-name "test skill" }
               (build-view-data-for-show-skill-view {:context @ctx :skill skill })))))
