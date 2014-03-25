(ns stockroom.admin.url-helper
  (:require [clojurewerkz.route-one.core :as route-one]))

(defprotocol UrlContext
  (host-uri [this])
  (url-prefix [this]))

(defn remove-trailing-slash [s]
  (loop [s s]
    (if (.endsWith s "/")
      (recur (.substring s 0 (dec (.length s))))
      s)))

(defn remove-leading-slash [s]
  (loop [s s]
    (if (.startsWith s "/")
      (recur (.substring s 1 (.length s)))
      s)))

(defn path-join [leading trailing]
  (let [leading (or leading "")
        trailing (or trailing "")]
    (str (remove-trailing-slash (or leading ""))
         "/"
         (remove-leading-slash (or trailing "")))))

(defn build-url [host-uri path]
  (path-join (if host-uri
               (str host-uri)
               "")
             path))

(defn url-for [context pattern params]
  (build-url (host-uri context)
             (path-join (url-prefix context)
                        (route-one/path-for pattern params))))

(defmacro defurl [^clojure.lang.Symbol n ^String pattern]
  `(defn ~n
     ([context#]
      (url-for context# ~pattern {}))
     ([context# params#]
      (url-for context# ~pattern params#))))

(defurl root-url "/")

(defurl list-apprenticeships-url "/apprenticeships")
(defurl new-apprenticeship-url "/apprenticeships/new")
(defurl create-apprenticeship-url "/apprenticeships")

(defurl create-user-url "/users")
(defurl list-users-url  "/users")
(defurl new-user-url    "/users/new")
(defurl show-user-url   "/users/:user-id")

(defurl create-client-url "/clients")
(defurl edit-client-url   "/clients/:client-id/edit")
(defurl list-clients-url  "/clients")
(defurl new-client-url    "/clients/new")
(defurl show-client-url   "/clients/:client-id")
(defurl update-client-url "/clients/:client-id")
(defurl delete-client-url "/clients/:client-id")

(defurl create-skill-url "/skills")
(defurl edit-skill-url   "/skills/:skill-id/edit")
(defurl list-skills-url  "/skills")
(defurl new-skill-url    "/skills/new")
(defurl show-skill-url   "/skills/:skill-id")
(defurl update-skill-url "/skills/:skill-id")

(defurl new-project-url    "/clients/:client-id/projects/new")
(defurl create-project-url "/clients/:client-id/projects")
(defurl edit-project-url   "/clients/:client-id/projects/:project-id/edit")
(defurl update-project-url "/clients/:client-id/projects/:project-id")
(defurl delete-project-url "/clients/:client-id/projects/:project-id")

(defurl new-sow-url    "/clients/:client-id/sows/new")
(defurl create-sow-url "/clients/:client-id/sows")
(defurl show-sow-url   "/clients/:client-id/sows/:sow-id")
(defurl edit-sow-url   "/clients/:client-id/sows/:sow-id/edit")
(defurl update-sow-url "/clients/:client-id/sows/:sow-id")
(defurl delete-sow-url "/clients/:client-id/sows/:sow-id")

(defurl list-employments-url  "/employments")
(defurl new-employment-url    "/employments/new")
(defurl create-employment-url "/employments/new")
(defurl edit-employment-url   "/employments/:employment-id/edit")
(defurl update-employment-url "/employments/:employment-id/edit")

(defurl delete-location-membership-url "/employments/:employment-id/location-memberships")
(defurl create-location-membership-url "/employments/:employment-id/location-memberships")
(defurl new-location-membership-url "/employments/:employment-id/location-memberships/new")

(defurl new-location-url    "/locations/new")
(defurl list-locations-url  "/locations")
(defurl show-location-url   "/locations/:location-id")
(defurl create-location-url "/locations/new")

(defurl create-group-url "/groups")
(defurl list-groups-url  "/groups")
(defurl new-group-url    "/groups/new")
(defurl show-group-url   "/groups/:group-id")

(defurl add-permission-url    "/groups/:group-id/permissions")
(defurl remove-permission-url "/groups/:group-id/permissions")

(defurl add-user-to-group-url      "/groups/:group-id/users")
(defurl remove-user-from-group-url "/groups/:group-id/users")

(defurl login-url "/login")
(defurl logout-url "/logout")

(defurl list-people-url "/people")
(defurl new-person-url "/people/new")
(defurl create-person-url "/people/new")
(defurl edit-person-url "/people/:person-id/edit")
(defurl update-person-url "/people/:person-id/edit")

(defurl google-oauth2-login-url    "/auth/google_oauth2")
(defurl google-oauth2-callback-url "/auth/google_oauth2/callback")
