(ns stockroom.admin.employment.list-employments
  (:require [hiccup.core :as hiccup]
            [ring.util.response :as response]
            [stockroom.admin.employment.index-view :refer [render-edit-url
                                                           render-employments-index-view
                                                           render-sortable-column-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [when-status]]
            [stockroom.admin.util.view-helper :refer [first-last-name
                                                      month-day-year]]
            [stockroom.v1.api :as api]
            [stockroom.v1.ring :as wring]))

(defn direction-urls-for [column query context]
  {:asc (urls/list-employments-url context (assoc query :sort (name column) :direction "asc"))
   :desc (urls/list-employments-url context (assoc query :sort (name column) :direction "desc"))})

(defn build-sortable-column-fn [query context]
  (fn [column-name column-label]
    {:value-view-key column-name
     :label-view (render-sortable-column-view column-name
                                              column-label
                                              (:sort query)
                                              (:direction query)
                                              (direction-urls-for column-name
                                                                  query
                                                                  context))}))

(defn build-view-data-for-employments-index-view [{:keys [employments query context]}]
  (let [build-sortable-column (build-sortable-column-fn query context)]
    {:new-employee-url (urls/new-employment-url context)
     :employments (map
                    (fn [{:keys [person position start end id]}]
                      (let [{:keys [first-name last-name]} person]
                        {:full-name (hiccup/h (first-last-name first-name last-name))
                         :position (hiccup/h (:name position))
                         :start (month-day-year start)
                         :end   (month-day-year end)
                         :edit-url (-> context
                                     (urls/edit-employment-url {:employment-id id})
                                     render-edit-url)}))
                    employments)
     :columns [(build-sortable-column :full-name "Name")
               (build-sortable-column :position "Position")
               (build-sortable-column :start "Start")
               (build-sortable-column :end "End")
               {:value-view-key :edit-url :label-view ""}]
     }))

(defn- validate-direction [query params]
  (assoc query
         :direction (case (:direction params)
                      "asc" :asc
                      "desc" :desc
                      nil)))

(defn- validate-sort [query params]
  (if-let [sort (:sort params)]
    (if-let [valid-sort (some #(when (= % sort) %) ["full-name" "position" "start" "end"])]
      (assoc query :sort (keyword valid-sort))
      query)
    query))

(defn- apply-search-defaults [query params]
  (if (and (:sort query) (:direction query))
    query
    (assoc query
           :sort :start
           :direction :asc)))

(defn build-search-query-from-params [params]
  (-> {}
    (validate-direction params)
    (validate-sort params)
    (apply-search-defaults params)))

(defn list-employments [context {:keys [params] :as request}]
  (let [query (build-search-query-from-params params)]
    (when-status
      :success
      (fn [api employments]
        (-> {:context context
             :query query
             :employments employments}
          build-view-data-for-employments-index-view
          render-employments-index-view
          response/response))
      (api/find-all-employments (wring/user-api request) query))))
