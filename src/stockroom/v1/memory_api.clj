(ns stockroom.v1.memory-api
  (:require [stockroom.v1.api :as api]
            [stockroom.v1.memory.users :as users]
            [stockroom.v1.memory.groups :as groups]
            [stockroom.v1.memory.clients :as clients]
            [stockroom.v1.memory.skills :as skills]
            [stockroom.v1.memory.projects :as projects]
            [stockroom.v1.memory.sows :as sows]
            [stockroom.v1.memory.project-sows :as project-sows]
            [stockroom.v1.memory.project-skills :as project-skills]
            [stockroom.v1.memory.people :as people]
            [stockroom.v1.memory.employments :as employments]
            [stockroom.v1.memory.engagements :as engagements]
            [stockroom.v1.memory.apprenticeships :as apprenticeships]
            [stockroom.v1.memory.director-engagements :as director-engagements]
            [stockroom.v1.memory.locations :as locations]
            [stockroom.v1.memory.location-memberships :as location-memberships]))

(defrecord V1MemoryApi [db]
  api/V1Api

  (find-all-users [api]                           [api]                                   (users/find-all-users api))
  (create-user-with-authentication!               [api user-data]                         (users/create-user-with-authentication! api user-data))
  (find-user-by-provider-and-uid                  [api provider uid]                      (users/find-user-by-provider-and-uid api provider uid))
  (add-authentication-to-user!                    [api user-id user-data]                 (users/add-authentication-to-user! api user-id user-data))
  (find-authentications-for-user                  [api user-id]                           (users/find-authentications-for-user api user-id))
  (find-user-by-id                                [api user-id]                           (users/find-user-by-id api user-id))
  (has-any-permission?                            [api user-id permissions]               (users/has-any-permission? api user-id permissions))
  (find-all-permissions-for-user                  [api user-id]                           (users/find-all-permissions-for-user api user-id))

  (create-permissions-group!                      [api group-data]                        (groups/create-permissions-group! api group-data))
  (find-all-permission-groups                     [api]                                   (groups/find-all-permission-groups api))
  (add-permission-to-group!                       [api options]                           (groups/add-permission-to-group! api options))
  (find-permissions-for-group                     [api group-id]                          (groups/find-permissions-for-group api group-id))
  (find-permission-group-by-id                    [api group-id]                          (groups/find-permission-group-by-id api group-id))
  (remove-permission-from-group!                  [api options]                           (groups/remove-permission-from-group! api options))
  (add-user-to-group!                             [api options]                           (groups/add-user-to-group! api options))
  (remove-user-from-group!                        [api options]                           (groups/remove-user-from-group! api options))
  (find-all-users-in-group                        [api group-id]                          (groups/find-all-users-in-group api group-id))
  (find-all-groups-for-user                       [api user-id]                           (groups/find-all-groups-for-user api user-id))

  (create-client!                                 [api options]                           (clients/create-client! api options))
  (update-client!                                 [api client-id options]                 (clients/update-client! api client-id options))
  (find-client-by-id                              [api client-id]                         (clients/find-client-by-id api client-id))
  (find-all-clients                               [api]                                   (clients/find-all-clients api))
  (delete-client!                                 [api client-id]                         (clients/delete-client! api client-id))

  (create-skill!                                  [api options]                           (skills/create-skill! api options))
  (update-skill!                                  [api skill-id options]                  (skills/update-skill! api skill-id options))
  (find-skill-by-id                               [api skill-id]                          (skills/find-skill-by-id api skill-id))
  (find-all-skills                                [api]                                   (skills/find-all-skills api))
  (find-all-skills-for-project                    [api project-id]                        (skills/find-all-skills-for-project api project-id))

  (create-project!                                [api options]                           (projects/create-project! api options))
  (update-project!                                [api project-id options]                (projects/update-project! api project-id options))
  (find-project-by-id                             [api project-id]                        (projects/find-project-by-id api project-id))
  (find-all-projects                              [api options]                           (projects/find-all-projects api options))
  (find-all-projects-for-client                   [api client-id]                         (projects/find-all-projects-for-client api client-id))
  (delete-project!                                [api project-id]                        (projects/delete-project! api project-id))

  (create-sow!                                    [api sow-data]                          (sows/create-sow! api sow-data))
  (update-sow!                                    [api sow-id sow-data]                   (sows/update-sow! api sow-id sow-data))
  (find-sow-by-id                                 [api sow-id]                            (sows/find-sow-by-id api sow-id))
  (find-all-sows                                  [api options]                           (sows/find-all-sows api options))
  (delete-sow!                                    [api sow-id]                            (sows/delete-sow! api sow-id))

  (create-project-sow!                            [api project-sow-data]                  (project-sows/create-project-sow! api project-sow-data))
  (update-project-sow!                            [api project-sow-id project-sow-data]   (project-sows/update-project-sow! api project-sow-id project-sow-data))
  (find-project-sow-by-id                         [api project-sow-id]                    (project-sows/find-project-sow-by-id api project-sow-id))
  (find-all-project-sows                          [api]                                   (project-sows/find-all-project-sows api))
  (delete-project-sow!                            [api project-sow-id]                    (project-sows/delete-project-sow! api project-sow-id))
  (delete-project-sows-for-sow!                   [api sow-id]                            (project-sows/delete-project-sows-for-sow! api sow-id))

  (create-project-skill!                          [api project-skill-data]                (project-skills/create-project-skill! api project-skill-data))
  (find-project-skill-by-id                       [api project-skill-id]                  (project-skills/find-project-skill-by-id api project-skill-id))
  (delete-project-skills-for-project!             [api project-id]                        (project-skills/delete-project-skills-for-project! api project-id))

  (create-person!                                 [api person-data]                       (people/create-person! api person-data))
  (update-person!                                 [api person-id person-data]             (people/update-person! api person-id person-data))
  (find-person-by-id                              [api person-id]                         (people/find-person-by-id api person-id))
  (find-all-people                                [api]                                   (people/find-all-people api))
  (search-people                                  [api criteria]                          (people/search-people api criteria))
  (find-current-people-by-position                [api position-name]                     (people/find-current-people-by-position api position-name))

  (create-employment-position!                    [api position-data]                     (employments/create-employment-position! api position-data))
  (find-all-employment-positions                  [api]                                   (employments/find-all-employment-positions api))
  (find-employment-position-by-id                 [api position-id]                       (employments/find-employment-position-by-id api position-id))
  (find-employment-position-by-name               [api position-name]                     (employments/find-employment-position-by-name api position-name))
  (create-employment!                             [api employment-data]                   (employments/create-employment! api employment-data))
  (update-employment!                             [api employment-id employment-data]     (employments/update-employment! api employment-id employment-data))
  (find-employment-by-id                          [api employment-id]                     (employments/find-employment-by-id api employment-id))
  (find-all-employments                           [api options]                           (employments/find-all-employments api options))
  (find-all-location-memberships-for-employment   [api employment-id]                     (employments/find-all-location-memberships-for-employment api employment-id))

  (create-engagement!                             [api engagement-data]                   (engagements/create-engagement! api engagement-data))
  (update-engagement!                             [api engagement-id engagement-data]     (engagements/update-engagement! api engagement-id engagement-data))
  (find-engagement-by-id                          [api engagement-id]                     (engagements/find-engagement-by-id api engagement-id))
  (delete-engagement!                             [api engagement-id]                     (engagements/delete-engagement! api engagement-id))
  (find-all-engagements                           [api options]                           (engagements/find-all-engagements api options))

  (create-apprenticeship!                         [api apprenticeship-data]               (apprenticeships/create-apprenticeship! api apprenticeship-data))
  (find-apprenticeship-by-id                      [api apprenticeship-id]                 (apprenticeships/find-apprenticeship-by-id api apprenticeship-id))
  (find-all-apprenticeships                       [api]                                   (apprenticeships/find-all-apprenticeships api))
  (upcoming-apprentice-graduations-by-location    [api]                                   (apprenticeships/upcoming-apprentice-graduations-by-location api))

  (create-director-engagement!                    [api data]                              (director-engagements/create-director-engagement! api data))
  (update-director-engagement!                    [api director-engagement-id data]       (director-engagements/update-director-engagement! api director-engagement-id data))
  (find-director-engagement-by-id                 [api director-engagement-id]            (director-engagements/find-director-engagement-by-id api director-engagement-id))
  (find-all-director-engagements-by-person-id     [api director-engagement-id]            (director-engagements/find-all-director-engagements-by-person-id api director-engagement-id))
  (find-current-directors                         [api]                                   (director-engagements/find-current-directors api))

  (create-location!                               [api location-data]                     (locations/create-location! api location-data))
  (find-location-by-id                            [api location-id]                       (locations/find-location-by-id api location-id))
  (find-all-locations                             [api]                                   (locations/find-all-locations api))

  (create-location-membership!                    [api employment-id location-id data]    (location-memberships/create-location-membership! api employment-id location-id data))
  (find-location-membership-by-id                 [api location-membership-id]            (location-memberships/find-location-membership-by-id api location-membership-id))
  (find-current-location-membership-for-people    [api employment-ids]                    (location-memberships/find-current-location-membership-for-people api employment-ids))
  (delete-location-membership!                    [api location-membership-id]            (location-memberships/delete-location-membership! api location-membership-id)))

(defn memory-api []
  (V1MemoryApi. {}))
