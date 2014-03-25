(ns stockroom.v1.authorized-api-spec
  (:require [speclj.core :refer :all]
            [stockroom.v1.api :refer [V1Api] :as api]
            [stockroom.v1.success-fn-api :refer [success-fn-api]]
            [stockroom.v1.authorized-api :refer [wrap-with-authorized-api]]
            [stockroom.spec-helper :refer [should-implement]]))

(defn api-with-permissions [stubbed-permissions]
  (success-fn-api
    {:find-all-permissions-for-user
     (fn [api user-id]
       {:status :success
        :api api
        :result stubbed-permissions})}))

(defmacro it-needs-permission [method permissions options]
  (let [stub-name (keyword method)
        method-name (name method)
        api-method (symbol "stockroom.v1.api" (name method))]
  `(list*
     (it (format "%s cannot be called without %s permission" ~method-name (clojure.string/join " or " ~permissions))
       (let [no-permissions-api# (wrap-with-authorized-api (api-with-permissions []) 1)
             without-permission-response# (~api-method no-permissions-api# ~@(:args options))]
         (should= :unauthorized (:status without-permission-response#))
         (should-be-nil (:result without-permission-response#))
         (should= no-permissions-api# (:api without-permission-response#))
         (should-not-have-invoked ~stub-name)))

     (for [allowed-permission# ~permissions]
       (it (format "%s can be called when current user has %s permission" ~method-name allowed-permission#)
         (let [raw-api#  (api-with-permissions [allowed-permission#])
               api-with-permissions# (wrap-with-authorized-api raw-api# 1)
               with-permission-response# (~api-method api-with-permissions# ~@(:args options))]
           (should-be-a (type api-with-permissions#) (:api with-permission-response#))
           (should= :success (:status with-permission-response#))
           (should-have-invoked ~stub-name)))))))

(describe "stockroom.api.authorized-api"

  (with-stubs)

  (it "implements the whole api"
    (should-implement V1Api (class (wrap-with-authorized-api nil 1))))

  (it-needs-permission create-user-with-authentication!
                       ["user-administration"]
                       {:args [:user-data]})

  (it-needs-permission find-user-by-provider-and-uid
                       ["user-administration" "user-administration/read-only"]
                       {:args [:provider :uid]})

  (it-needs-permission add-authentication-to-user!
                       ["user-administration"]
                       {:args [:provider :uid]})

  (it-needs-permission find-authentications-for-user
                       ["user-administration" "user-administration/read-only"]
                       {:args [:user-id]})

  (it-needs-permission find-user-by-id
                       ["user-administration" "user-administration/read-only"]
                       {:args [:user-id]})

  (it-needs-permission create-permissions-group!
                       ["user-administration"]
                       {:args [:group-data]})

  (it-needs-permission find-all-permission-groups
                       ["user-administration" "user-administration/read-only"]
                       {:args []})

  (it-needs-permission add-permission-to-group!
                       ["user-administration"]
                       {:args [:group-id]})

  (it-needs-permission find-permissions-for-group
                       ["user-administration" "user-administration/read-only"]
                       {:args [:group-id]})

  (it-needs-permission find-permission-group-by-id
                       ["user-administration" "user-administration/read-only"]
                       {:args [:group-id]})

  (it-needs-permission find-permission-group-by-id
                       ["user-administration" "user-administration/read-only"]
                       {:args [:group-id]})

  (it-needs-permission add-user-to-group!
                       ["user-administration"]
                       {:args [:options]})

  (it-needs-permission remove-permission-from-group!
                       ["user-administration"]
                       {:args [:options]})

  (it-needs-permission remove-user-from-group!
                       ["user-administration"]
                       {:args [:options]})

  (it-needs-permission find-all-users-in-group
                       ["user-administration" "user-administration/read-only"]
                       {:args [:group-id]})

  (it-needs-permission find-all-users
                       ["user-administration" "user-administration/read-only"]
                       {:args []})

  (it-needs-permission find-all-groups-for-user
                       ["user-administration" "user-administration/read-only"]
                       {:args [:user-id]})

  ;(it-needs-permission has-any-permission?
  ;                     ["user-administration"]
  ;                     {:args [:user-id :permissions]})

  ;(it-needs-permission find-all-permissions-for-user
  ;                     ["user-administration" "user-administration/read-only"]
  ;                     {:args [:user-id]})

  (it-needs-permission create-client!
                       ["clients"]
                       {:args [:options]})

  (it-needs-permission update-client!
                       ["clients"]
                       {:args [:client-id :options]})

  (it-needs-permission find-client-by-id
                       ["clients" "clients/read-only"]
                       {:args [:client-id]})

  (it-needs-permission find-all-clients
                       ["clients" "clients/read-only"]
                       {:args []})

  (it-needs-permission create-skill!
                       ["skills"]
                       {:args [:options]})

  (it-needs-permission delete-client!
                       ["clients"]
                       {:args [:client-id]})

  (it-needs-permission update-skill!
                       ["skills"]
                       {:args [:skill-id :options]})

  (it-needs-permission find-skill-by-id
                       ["skills" "skills/read-only"]
                       {:args [:skill-id]})

  (it-needs-permission find-all-skills
                       ["skills" "skills/read-only"]
                       {:args []})

  (it-needs-permission find-all-skills-for-project
                       ["skills" "skills/read-only"]
                       {:args [:project-id]})

  (it-needs-permission create-project!
                       ["projects"]
                       {:args [:options]})

  (it-needs-permission update-project!
                       ["projects"]
                       {:args [:project-id :options]})

  (it-needs-permission find-project-by-id
                       ["projects" "projects/read-only"]
                       {:args [:project-id]})

  (it-needs-permission find-all-projects
                       ["projects" "projects/read-only"]
                       {:args [:options]})

  (it-needs-permission find-all-projects-for-client
                       ["projects" "projects/read-only"]
                       {:args [:client]})

  (it-needs-permission delete-project!
                       ["projects"]
                       {:args [:project-id]})

  (it-needs-permission create-sow!
                       ["sows"]
                       {:args [:sow-data]})

  (it-needs-permission update-sow!
                       ["sows"]
                       {:args [:sow-id :options]})

  (it-needs-permission find-sow-by-id
                       ["sows" "sows/read-only"]
                       {:args [:project-id]})

  (it-needs-permission find-all-sows
                       ["sows" "sows/read-only"]
                       {:args [:options]})

  (it-needs-permission delete-sow!
                       ["sows"]
                       {:args [:sow-id]})

  (it-needs-permission create-project-sow!
                       ["project-sows"]
                       {:args [:project-sow-data]})

  (it-needs-permission update-project-sow!
                       ["project-sows"]
                       {:args [:project-sow-id :options]})

  (it-needs-permission find-project-sow-by-id
                       ["project-sows" "project-sows/read-only"]
                       {:args [:project-sow-id]})

  (it-needs-permission find-all-project-sows
                       ["project-sows" "project-sows/read-only"]
                       {:args []})

  (it-needs-permission delete-project-sow!
                       ["project-sows"]
                       {:args [:project-sow-id]})

  (it-needs-permission delete-project-sows-for-sow!
                       ["project-sows"]
                       {:args [:sow-id]})

  (it-needs-permission create-project-skill!
                       ["project-skills"]
                       {:args [:project-skill-data]})

  (it-needs-permission find-project-skill-by-id
                       ["project-skills" "project-skills/read-only"]
                       {:args [:project-id]})

  (it-needs-permission delete-project-skills-for-project!
                       ["project-skills"]
                       {:args [:project-id]})

  (it-needs-permission find-all-director-engagements-by-person-id
                       ["engagements" "engagements/read-only"]
                       {:args [:director-id]})

  (it-needs-permission create-employment-position!
                       ["employees"]
                       {:args [:position-data]})

  (it-needs-permission find-all-employment-positions
                       ["employees" "employees/read-only"]
                       {:args []})

  (it-needs-permission find-employment-position-by-id
                       ["employees" "employees/read-only"]
                       {:args [:position-id]})

  (it-needs-permission create-employment!
                       ["employees"]
                       {:args [:employment-data]})

  (it-needs-permission update-employment!
                       ["employees"]
                       {:args [:employment-id :employment-data]})

  (it-needs-permission find-employment-by-id
                       ["employees" "employees/read-only"]
                       {:args [:employment-id]})

  (it-needs-permission find-all-employments
                       ["employees" "employees/read-only"]
                       {:args [:options]})

  (it-needs-permission find-all-location-memberships-for-employment
                       ["employees" "employees/read-only"]
                       {:args [:options]})

  (it-needs-permission create-person!
                       ["people"]
                       {:args [:options]})

  (it-needs-permission update-person!
                       ["people"]
                       {:args [:person-id :options]})

  (it-needs-permission find-person-by-id
                       ["people" "people/read-only"]
                       {:args [:options]})

  (it-needs-permission create-engagement!
                       ["engagements"]
                       {:args [:options]})

  (it-needs-permission update-engagement!
                       ["engagements"]
                       {:args [:engagement-id :options]})

  (it-needs-permission delete-engagement!
                       ["engagements"]
                       {:args [:engagement-id]})

  (it-needs-permission find-engagement-by-id
                       ["engagements" "engagements/read-only"]
                       {:args [:engagement-id]})

  (it-needs-permission find-all-engagements
                       ["engagements" "engagements/read-only"]
                       {:args [:options]})

  (it-needs-permission create-apprenticeship!
                       ["apprenticeships"]
                       {:args [:options]})

  (it-needs-permission find-apprenticeship-by-id
                       ["apprenticeships" "apprenticeships/read-only"]
                       {:args [:apprenticeship-id]})

  (it-needs-permission find-all-apprenticeships
                       ["apprenticeships" "apprenticeships/read-only"]
                       {:args []})

  (it-needs-permission upcoming-apprentice-graduations-by-location
                       ["apprenticeships" "apprenticeships/read-only"]
                       {:args []})

  (it-needs-permission create-director-engagement!
                       ["engagements"]
                       {:args [:director-engagement-data]})

  (it-needs-permission update-director-engagement!
                       ["engagements"]
                       {:args [:director-engagement-id :director-engagement-data]})

  (it-needs-permission find-director-engagement-by-id
                       ["engagements" "engagements/read-only"]
                       {:args [:director-engagement-id]})

  (it-needs-permission find-current-directors
                       ["people" "people/read-only" "engagements" "engagements/read-only"]
                       {:args []})

  (it-needs-permission create-location!
                       ["locations"]
                       {:args [:location-data]})

  (it-needs-permission find-all-locations
                       ["locations" "locations/read-only"]
                       {:args []})

  (it-needs-permission find-location-by-id
                       ["locations" "locations/read-only"]
                       {:args [:location-id]})

  (it-needs-permission create-location-membership!
                       ["location-memberships"]
                       {:args [:person-id :locationi-id :location-membership-data]})

  (it-needs-permission find-location-membership-by-id
                       ["location-memberships" "location-memberships/read-only"]
                       {:args [:location-membership-id]})

  (it-needs-permission delete-location-membership!
                       ["location-memberships"]
                       {:args [:location-membership-id]}))
