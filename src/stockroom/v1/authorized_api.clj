(ns stockroom.v1.authorized-api
  (:require [clojure.set :as set]
            [stockroom.v1.api :as protocol :refer [V1Api]]))

(def method-permissions
  {:create-user-with-authentication!                ["user-administration"]
   :find-user-by-provider-and-uid                   ["user-administration" "user-administration/read-only"]
   :add-authentication-to-user!                     ["user-administration"]
   :find-authentications-for-user                   ["user-administration" "user-administration/read-only"]
   :find-user-by-id                                 ["user-administration" "user-administration/read-only"]
   :create-permissions-group!                       ["user-administration"]
   :find-all-permission-groups                      ["user-administration" "user-administration/read-only"]
   :add-permission-to-group!                        ["user-administration"]
   :find-permissions-for-group                      ["user-administration" "user-administration/read-only"]
   :find-permission-group-by-id                     ["user-administration" "user-administration/read-only"]
   :add-user-to-group!                              ["user-administration"]
   :remove-user-from-group!                         ["user-administration"]
   :find-all-users-in-group                         ["user-administration" "user-administration/read-only"]
   :find-all-users                                  ["user-administration" "user-administration/read-only"]
   :find-all-groups-for-user                        ["user-administration" "user-administration/read-only"]
   :remove-permission-from-group!                   ["user-administration"]
   :has-any-permission?                             ["user-administration"]
   :find-all-permissions-for-user                   ["user-administration"]

   :create-client!                                  ["clients"]
   :update-client!                                  ["clients"]
   :find-client-by-id                               ["clients" "clients/read-only"]
   :find-all-clients                                ["clients" "clients/read-only"]
   :delete-client!                                  ["clients"]

   :create-skill!                                   ["skills"]
   :update-skill!                                   ["skills"]
   :find-skill-by-id                                ["skills" "skills/read-only"]
   :find-all-skills                                 ["skills" "skills/read-only"]
   :find-all-skills-for-project                     ["skills" "skills/read-only"]

   :create-project!                                 ["projects"]
   :update-project!                                 ["projects"]
   :find-project-by-id                              ["projects" "projects/read-only"]
   :find-all-projects                               ["projects" "projects/read-only"]
   :find-all-projects-for-client                    ["projects" "projects/read-only"]
   :delete-project!                                 ["projects"]

   :create-sow!                                     ["sows"]
   :update-sow!                                     ["sows"]
   :find-sow-by-id                                  ["sows" "sows/read-only"]
   :find-all-sows                                   ["sows" "sows/read-only"]
   :delete-sow!                                     ["sows"]

   :create-project-sow!                             ["project-sows"]
   :update-project-sow!                             ["project-sows"]
   :find-project-sow-by-id                          ["project-sows" "project-sows/read-only"]
   :find-all-project-sows                           ["project-sows" "project-sows/read-only"]
   :delete-project-sow!                             ["project-sows"]
   :delete-project-sows-for-sow!                    ["project-sows"]

   :create-project-skill!                           ["project-skills"]
   :find-project-skill-by-id                        ["project-skills" "project-skills/read-only"]
   :delete-project-skills-for-project!              ["project-skills"]

   :create-person!                                  ["people"]
   :update-person!                                  ["people"]
   :find-person-by-id                               ["people" "people/read-only"]
   :find-all-people                                 ["people" "people/read-only"]
   :search-people                                   ["people" "people/read-only"]
   :find-current-people-by-position                 ["people" "people/read-only"]

   :create-employment-position!                     ["employees"]
   :find-employment-position-by-id                  ["employees" "employees/read-only"]
   :find-employment-position-by-name                ["employees" "employees/read-only"]
   :find-all-employment-positions                   ["employees" "employees/read-only"]

   :create-employment!                              ["employees"]
   :update-employment!                              ["employees"]
   :find-employment-by-id                           ["employees" "employees/read-only"]
   :find-all-employments                            ["employees" "employees/read-only"]
   :find-all-location-memberships-for-employment    ["employees" "employees/read-only"]

   :create-engagement!                              ["engagements"]
   :update-engagement!                              ["engagements"]
   :delete-engagement!                              ["engagements"]
   :find-engagement-by-id                           ["engagements" "engagements/read-only"]
   :find-all-engagements                            ["engagements" "engagements/read-only"]

   :create-apprenticeship!                          ["apprenticeships"]
   :find-apprenticeship-by-id                       ["apprenticeships" "apprenticeships/read-only"]
   :find-all-apprenticeships                        ["apprenticeships" "apprenticeships/read-only"]
   :upcoming-apprentice-graduations-by-location     ["apprenticeships" "apprenticeships/read-only"]

   :create-director-engagement!                     ["engagements"]
   :update-director-engagement!                     ["engagements"]
   :find-director-engagement-by-id                  ["engagements" "engagements/read-only"]
   :find-all-director-engagements-by-person-id      ["engagements" "engagements/read-only"]

   :find-current-directors                          ["people" "people/read-only" "engagements" "engagements/read-only"]

   :create-location!                                ["locations"]
   :find-all-locations                              ["locations" "locations/read-only"]
   :find-location-by-id                             ["locations" "locations/read-only"]

   :create-location-membership!                           ["location-memberships"]
   :find-location-membership-by-id                        ["location-memberships" "location-memberships/read-only"]
   :find-current-location-membership-for-people           ["location-memberships" "location-memberships/read-only"]
   :delete-location-membership!                           ["location-memberships"]})

(defmacro def-wrapped-api [type -methods-to-implement]
  (let [methods-to-implement (eval -methods-to-implement)
        sigs (select-keys (:sigs V1Api) methods-to-implement)
        methods (reduce
                  (fn [methods [method-name sig]]
                    (reduce
                      (fn [methods arglist]
                        (let [protocol-method (symbol "stockroom.v1.api" (name method-name))]
                          (conj methods
                                `(~(:name sig) ~arglist
                                     (~'wrapper ~method-name ~protocol-method ~@arglist)))))
                      methods
                      (:arglists sig)))
                  []
                  sigs)]
    (list* 'deftype type ['wrapper] 'V1Api methods)))

(def-wrapped-api AuthorizedApi (keys method-permissions))

(defn has-any-permissions? [needs-one-of-these-permissions user-permissions]
  (-> (set/intersection needs-one-of-these-permissions user-permissions)
    seq
    boolean))

(defn unauthorized [api]
  {:status :unauthorized :api api})

(defn wrap-method-calls-with-authorization [wrapped-api current-user-id]
  (let [user-permissions (delay (-> (protocol/find-all-permissions-for-user wrapped-api current-user-id) :result set))]
    (fn [method-name-kw protocol-method api & args]
      (let [needed-permissions (set (get method-permissions method-name-kw))
            permitted? (has-any-permissions? needed-permissions @user-permissions)]
        (if permitted?
          (let [{:keys [api] :as response} (apply protocol-method wrapped-api args)
                wrapped-api (AuthorizedApi. (wrap-method-calls-with-authorization api current-user-id))]
            (assoc response :api wrapped-api))
          {:status :unauthorized :api api})))))

(defn wrap-with-authorized-api [api current-user-id]
  (AuthorizedApi. (wrap-method-calls-with-authorization api current-user-id)))
