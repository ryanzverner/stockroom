(ns stockroom.admin.groups.show-view
  (:require [hiccup.core :refer [h]]
            [hiccup.def :refer [defhtml]]
            [hiccup.form :refer [form-to select-options]]))

(defhtml render-show-group-view [view-data]
  (let [can-add-permission? (:can-add-permission? view-data)
        show-group-permissions? (:show-group-permissions? view-data)
        can-add-users? (:can-add-users? view-data)
        show-group-users? (:show-group-users? view-data)
        errors (:errors view-data)]
    [:div
     [:h1 (h (:group-name view-data)) " - " [:span " Control Panel"]]

     (when (or can-add-permission? show-group-permissions?)
       [:div.row
        [:section.column.half



        [:h2 "Permissions"]
        (when show-group-permissions?
          [:table
           [:thead
            [:tr
             [:td "Permission"]
             [:td]]]
           [:tbody
            (for [permission-name (:permissions-in-group view-data)]
              [:tr
               [:td permission-name]
               [:td (form-to [:delete (:remove-permission-url view-data)]
                             [:input {:type :hidden :name "permission" :value permission-name}]
                             [:input.caution {:type :submit :value "Remove"}])]])]])

        [:br ]

        (when can-add-permission?
          (form-to [:post (:add-permission-to-group-url view-data)]
                   [:label "Add permission "]
                   (when-let [errors (:permission errors)]
                     [:ul (for [error errors]
                            [:li error])])
                   [:select {:name "permission"}
                    (select-options (:add-permission-options view-data) nil)]
                   [:input {:type :submit :value "Submit"}]))

        (when-not can-add-permission?
          [:p.notice "There are no more permissions to add"])

        ]])

     (when (or can-add-users? show-group-users?)
       [:div.row
        [:section.column.half

        [:h2 "Current members"]
        (when show-group-users?
          [:table
           [:thead
            [:tr
             [:td "Name"]
             [:td]]]
           [:tbody
            (for [{:keys [user-url display remove-value]} (:users-in-group view-data)]
              [:tr
               [:td [:a {:href user-url} display]]
               [:td (form-to [:delete (:remove-user-from-group-url view-data)]
                             [:input {:type :hidden :name "user-id" :value remove-value}]
                             [:input.caution {:type :submit :value "Remove"}])]])]])

        (when can-add-users?
          (form-to [:post (:add-user-to-group-url view-data)]
                   [:label "Add user "]
                   (when-let [errors (:user errors)]
                     [:ul (for [error errors]
                            [:li error])])
                   [:select {:name "user-id"}
                    (select-options (:add-user-options view-data) nil)]
                   [:input {:type :submit :value "Submit"}]))
        [:br]

        (when-not can-add-users?
          [:p.notice "There are no more users to add to the current group"])


        ]]
       )

     ]))
