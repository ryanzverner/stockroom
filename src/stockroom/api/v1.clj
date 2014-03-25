(ns stockroom.api.v1
  (:require [compojure.core :refer [defroutes make-route]]
            [stockroom.api.v1.clients :refer [list-clients
                                              show-client
                                              create-client
                                              update-client]]
            [stockroom.api.v1.skills :refer [list-skills
                                             show-skill
                                             create-skill
                                             update-skill]]
            [stockroom.api.v1.employments :refer [create-employment
                                                  list-employments
                                                  show-employment
                                                  update-employment]]
            [stockroom.api.v1.director-engagements :refer [create-director-engagement
                                                           update-director-engagement
                                                           show-director-engagement]]
            [stockroom.api.v1.engagements :refer [create-engagement
                                                  delete-engagement
                                                  list-engagements
                                                  show-engagement
                                                  update-engagement]]
            [stockroom.api.v1.people :refer [create-person
                                             show-person
                                             search-people]]
            [stockroom.api.v1.directors :refer [list-current-directors
                                                list-director-engagements]]
            [stockroom.api.v1.projects :refer [list-projects
                                               show-project
                                               create-project
                                               update-project]]
            [stockroom.api.v1.locations :refer [list-locations
                                                show-location
                                                create-location]]
            [stockroom.api.v1.apprenticeships :refer [list-apprenticeships
                                                      show-apprenticeship
                                                      create-apprenticeship
                                                      upcoming-apprentice-graduations-by-location]]
            [stockroom.api.v1.craftsmen :refer [list-current-craftsmen]]
            [stockroom.api.v1.me :refer [list-my-permissions]]))

(defroutes handler
  (make-route :get    "/v1/clients" list-clients)
  (make-route :get    "/v1/clients/:client-id" show-client)
  (make-route :post   "/v1/clients" create-client)
  (make-route :put    "/v1/clients/:client-id" update-client)
  (make-route :get    "/v1/skills" list-skills)
  (make-route :get    "/v1/skills/:skill-id" show-skill)
  (make-route :post   "/v1/skills" create-skill)
  (make-route :put    "/v1/skills/:skill-id" update-skill)
  (make-route :get    "/v1/projects" list-projects)
  (make-route :get    "/v1/projects/:project-id" show-project)
  (make-route :post   "/v1/projects" create-project)
  (make-route :put    "/v1/projects/:project-id" update-project)
  (make-route :get    "/v1/employments" list-employments)
  (make-route :post   "/v1/employments" create-employment)
  (make-route :get    "/v1/employments/:employment-id" show-employment)
  (make-route :put    "/v1/employments/:employment-id" update-employment)
  (make-route :post   "/v1/director-engagements" create-director-engagement)
  (make-route :get    "/v1/director-engagements/:director-engagement-id" show-director-engagement)
  (make-route :put    "/v1/director-engagements/:director-engagement-id" update-director-engagement)
  (make-route :get    "/v1/engagements" list-engagements)
  (make-route :get    "/v1/engagements/:engagement-id" show-engagement)
  (make-route :post   "/v1/engagements" create-engagement)
  (make-route :put    "/v1/engagements/:engagement-id" update-engagement)
  (make-route :delete "/v1/engagements/:engagement-id" delete-engagement)
  (make-route :post   "/v1/people" create-person)
  (make-route :get    "/v1/people/search" search-people)
  (make-route :get    "/v1/people/:person-id" show-person)
  (make-route :get    "/v1/directors/current" list-current-directors)
  (make-route :get    "/v1/directors/:director-id/director-engagements" list-director-engagements)
  (make-route :get    "/v1/craftsmen/current" list-current-craftsmen)
  (make-route :get    "/v1/apprenticeships" list-apprenticeships)
  (make-route :get    "/v1/apprenticeships/graduations" upcoming-apprentice-graduations-by-location)
  (make-route :get    "/v1/apprenticeships/:apprenticeship-id" show-apprenticeship)
  (make-route :post   "/v1/apprenticeships" create-apprenticeship)
  (make-route :get    "/v1/me/permissions" list-my-permissions)
  (make-route :get    "/v1/locations" list-locations)
  (make-route :get    "/v1/locations/:location-id" show-location)
  (make-route :post   "/v1/locations" create-location))
