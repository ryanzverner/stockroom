(ns stockroom.admin.employment.list-employments-spec
  (:require [chee.datetime :refer [days-ago]]
            [speclj.core :refer :all]
            [stockroom.admin.employment.index-view :as index-view]
            [stockroom.admin.employment.list-employments :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.view-helper :refer [month-day-year]]))

(def ten-days-ago (days-ago 10))
(def five-days-ago (days-ago 1))
(def four-days-ago (days-ago 4))
(def three-days-ago (days-ago 3))

(defn test-employments []
  [{:id 9
    :start ten-days-ago
    :end four-days-ago
    :position-id 10
    :position {:id 10
               :name "<bad-tag>Developer"}
    :person-id 11
    :person {:id 11
             :first-name "<p>John"
             :last-name  "Smith"}}
   {:id 40
    :start five-days-ago
    :end three-days-ago
    :position-id 50
    :position {:id 50
               :name "Admin"}
    :person-id 60
    :person {:id 60
             :first-name "Sally"
             :last-name  "Jones"}}
   ])

(describe "stockroom.admin.employment.list-employments"

  (with api (test-stockroom-api))
  (with ctx (test-admin-context))

  (it "builds view data for the employments index view"
    (let [query {:sort :full-name :direction :asc}
          build-sortable-column (build-sortable-column-fn query @ctx)]
      (should= {:new-employee-url (urls/new-employment-url @ctx)
                :employments [{:full-name "&lt;p&gt;John Smith"
                               :position "&lt;bad-tag&gt;Developer"
                               :start (month-day-year ten-days-ago)
                               :end (month-day-year four-days-ago)
                               :edit-url (index-view/render-edit-url (urls/edit-employment-url @ctx {:employment-id 9}))
                               }
                              {:full-name "Sally Jones"
                               :position "Admin"
                               :start (month-day-year five-days-ago)
                               :end (month-day-year three-days-ago)
                               :edit-url (index-view/render-edit-url (urls/edit-employment-url @ctx {:employment-id 40}))}
                              ]
                :columns [(build-sortable-column :full-name "Name")
                          (build-sortable-column :position "Position")
                          (build-sortable-column :start "Start")
                          (build-sortable-column :end "End")
                          {:value-view-key :edit-url :label-view ""}]
                }
               (build-view-data-for-employments-index-view {:employments (test-employments)
                                                            :query query
                                                            :context @ctx}))))

  (context "builds a seach query from params"

    (with sort-defaults {:sort :start :direction :asc})

    (it "applies defaults"
      (should= @sort-defaults
               (build-search-query-from-params {})))

    (it "sets the sort to :desc when the param for sort is desc"
      (should= {:sort :start :direction :desc}
               (build-search-query-from-params {:sort "start" :direction "desc"})))

    (it "sets the sort to :asc when the param for sort is desc"
      (should= {:sort :end :direction :asc}
               (build-search-query-from-params {:sort "end" :direction "asc"})))

    (it "sets the sort defaults when the direction is present but the sort is not present"
      (should= @sort-defaults
               (build-search-query-from-params {:direction "desc"})))

    (it "sets the sort defaults when the sort is present but the direction is not present"
      (should= @sort-defaults
               (build-search-query-from-params {:sort "end"})))

    (it "sets the sort defaults when the sort column is not valid"
      (should= @sort-defaults
               (build-search-query-from-params {:sort "unknown" :direction "desc"})))

    )

  )

