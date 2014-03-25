(ns stockroom.admin.groups.routes
  (:require [clojure.string :as string]
            [compojure.core :refer [DELETE GET POST context]]
            [ring.util.response :as response]
            [stockroom.admin.groups.index-view :refer [render-group-index-view]]
            [stockroom.admin.groups.new-view :refer [render-new-group-view]]
            [stockroom.admin.groups.show-view :refer [render-show-group-view]]
            [stockroom.admin.url-helper :as urls]
            [stockroom.admin.util.response :refer [api-errors-to-web-errors
                                                   when-status]]
            [stockroom.v1.api :as api]
            [stockroom.v1.permissions :as permissions]
            [stockroom.v1.ring :as wring]))

(defn build-view-data-for-group-index [{:keys [all-groups context]}]
  {:groups (map
             (fn [g]
               {:name (:name g)
                :url (urls/show-group-url context {:group-id (:id g)})})
             all-groups)
   :new-group-url (urls/new-group-url context)})

(defn list-groups [context request]
  (let [api (wring/user-api request)]
    (when-status
      :success
      (fn [api groups]
        (-> (build-view-data-for-group-index {:context context
                                              :all-groups groups})
          render-group-index-view
          response/response))
      (api/find-all-permission-groups api))))

(defn build-view-data-for-group-show [{:keys [context group permissions-in-group
                                              all-permissions errors users-in-group
                                              all-users]}]
  (let [permissions-that-can-be-added (remove (set permissions-in-group) all-permissions)
        users-that-can-be-added (remove (set users-in-group) all-users)
        group-id (:id group)]
    {
     :group-name (:name group)
     :errors errors

     :can-add-permission? (boolean (seq permissions-that-can-be-added))
     :show-group-permissions? (boolean (seq permissions-in-group))
     :add-permission-options (cons ["Select a permission" ""]
                                   (map (fn [p] [p p]) permissions-that-can-be-added))
     :add-permission-to-group-url (urls/add-permission-url context {:group-id group-id})
     :remove-permission-url (urls/remove-permission-url context {:group-id group-id})
     :permissions-in-group permissions-in-group

     :can-add-users? (boolean (seq users-that-can-be-added))
     :show-group-users? (boolean (seq users-in-group))
     :add-user-options (cons ["Select a user" ""]
                             (map (fn [u] [(:name u) (:id u)]) users-that-can-be-added))
     :remove-user-from-group-url (urls/remove-user-from-group-url context {:group-id group-id})
     :add-user-to-group-url (urls/add-user-to-group-url context {:group-id group-id})
     :users-in-group (map
                       (fn [u]
                         {:user-url (urls/show-user-url context {:user-id (:id u)})
                          :display (:name u)
                          :remove-value (:id u)})
                       users-in-group)
     }))

(defn respond-with-show-group-view [{:keys [request context response-status group-id errors]}]
  (when-status
    :success
    (fn [api group]
      (when-status
        :success
        (fn [api permissions-in-group]
          (when-status
            :success
            (fn [api users-in-group]
              (when-status
                :success
                (fn [api all-users]
                  (-> (build-view-data-for-group-show {:group group
                                                       :errors errors
                                                       :context context
                                                       :permissions-in-group permissions-in-group
                                                       :all-permissions permissions/all-permissions
                                                       :users-in-group users-in-group
                                                       :all-users all-users})
                    render-show-group-view
                    response/response
                    (response/status response-status)
                    (wring/set-user-api api)))
                (api/find-all-users api)))
            (api/find-all-users-in-group api group-id)))
        (api/find-permissions-for-group api group-id)))
    (api/find-permission-group-by-id (wring/user-api request) group-id)))

(defn show-group [context request]
  (respond-with-show-group-view {:request request
                                 :context context
                                 :response-status 200
                                 :group-id (-> request :params :group-id)
                                 :errors {}}))

(defn build-view-data-for-new-group-view [{:keys [context errors]}]
  {:errors errors
   :create-group-url (urls/create-group-url context)})

(defn respond-with-new-group-view [{:keys [request response-status] :as options}]
  (-> options
    build-view-data-for-new-group-view
    render-new-group-view
    response/response
    (response/status response-status)
    (wring/set-user-api (wring/user-api request))))

(defn new-group [context request]
  (respond-with-new-group-view {:request request
                                :context context
                                :errors {}
                                :response-status 200}))

(defn validate-create-group-request [{:keys [name] :as params}]
  (if (and name (not (string/blank? name)))
    {}
    {:name ["Please enter a name for the group."]}))

(def duplicate-group-name-error "This group name is already taken. Please choose another name.")

(def group-api-error-mapping
  {:group/duplicate {:key :name
                     :message duplicate-group-name-error}})

(defn translate-api-errors [errors]
  (api-errors-to-web-errors errors group-api-error-mapping))

(defn create-group [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        errors (validate-create-group-request params)]
    (if (seq errors)
      (respond-with-new-group-view {:request request
                                    :context context
                                    :errors errors
                                    :response-status 422})
      (when-status
        :success
        (fn [api group-id]
          (-> (response/redirect-after-post (urls/list-groups-url context))
            (assoc-in [:flash :success] "Successfully created group.")
            (wring/set-user-api api)))
        :failure
        (fn [api errors]
          (let [errors (translate-api-errors errors)]
            (respond-with-new-group-view {:request request
                                          :context context
                                          :response-status 422
                                          :errors errors})))
        (api/create-permissions-group! api {:name (:name params)})))))

(defn valid-permission? [permission]
  (boolean (permissions/all-permissions permission)))

(defn validate-add-permission [params]
  (let [permission (:permission params)]
    (cond
      (or (nil? permission) (string/blank? permission))
      {:permission ["Please choose a permission."]}
      (not (valid-permission? permission))
      {:permission ["Please choose a valid permission."]})))

(defn add-permission [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [group-id permission]} params
        errors (validate-add-permission params)]
    (if (seq errors)
      (respond-with-show-group-view {:request request
                                     :context context
                                     :response-status 422
                                     :group-id group-id
                                     :errors errors})
      (when-status
        :success
        (fn [api _]
          (-> (response/redirect-after-post (urls/show-group-url context {:group-id group-id}))
            (assoc-in [:flash :success] (format "Added \"%s\" permission." permission))
            (wring/set-user-api api)))
        (api/add-permission-to-group! api {:group-id group-id :permission permission})))))

(defn remove-permission [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [group-id permission]} params]
    (when-status
      :success
      (fn [api _]
        (-> (response/redirect-after-post (urls/show-group-url context {:group-id group-id}))
          (assoc-in [:flash :success] (format "Removed permission \"%s\"." permission))
          (wring/set-user-api api)))
      (api/remove-permission-from-group! api {:group-id group-id
                                              :permission permission}))))

(defn validate-add-user-request [{:keys [user-id] :as params}]
  (if (or (nil? user-id) (string/blank? user-id))
    {:user ["Please select a user."]}
    {}))

(defn add-user-to-group [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [group-id user-id]} params
        errors (validate-add-user-request params)]
    (if (seq errors)
      (respond-with-show-group-view {:request request
                                     :context context
                                     :response-status 422
                                     :group-id group-id
                                     :errors errors})
      (when-status
        :success
        (fn [api _]
          (-> (response/redirect-after-post (urls/show-group-url context {:group-id group-id}))
            (assoc-in [:flash :success] "Added user.")
            (wring/set-user-api api)))
        (api/add-user-to-group! api {:group-id group-id
                                     :user-id user-id})))))

(defn remove-user-from-group [context {:keys [params] :as request}]
  (let [api (wring/user-api request)
        {:keys [group-id user-id return-url]} params]
    (when-status
      :success
      (fn [api _]
        (-> (response/redirect-after-post
              (or return-url (urls/show-group-url context {:group-id group-id})))
          (assoc-in [:flash :success] "Removed user.")
          (wring/set-user-api api)))
      (api/remove-user-from-group! api {:group-id group-id
                                        :user-id  user-id}))))

(defn handler [ctx]
  (context "/groups" []
    (GET  "/"    request (list-groups ctx request))
    (POST "/"    request (create-group ctx request))
    (GET  "/new" request (new-group ctx request))

    (context "/:group-id" []
      (GET    "/"            request (show-group ctx request))
      (POST   "/users"       request (add-user-to-group ctx request))
      (DELETE "/users"       request (remove-user-from-group ctx request))
      (POST   "/permissions" request (add-permission ctx request))
      (DELETE "/permissions" request (remove-permission ctx request)))))
