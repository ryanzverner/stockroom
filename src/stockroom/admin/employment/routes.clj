(ns stockroom.admin.employment.routes
  (:require [compojure.core :refer [GET POST PUT DELETE context]]
            [stockroom.admin.employment.create-employment :refer [create-employment]]
            [stockroom.admin.employment.edit-employment :refer [edit-employment]]
            [stockroom.admin.employment.list-employments :refer [list-employments]]
            [stockroom.admin.employment.new-employment :refer [new-employment]]
            [stockroom.admin.employment.update-employment :refer [update-employment]]
            [stockroom.admin.location-memberships.delete-location-membership :refer [delete-location-membership]]
            [stockroom.admin.location-memberships.new-location-membership :refer [new-location-membership]]
            [stockroom.admin.location-memberships.create-location-membership :refer [create-location-membership]]))

(defn handler [ctx]
  (context "/employments" []
    (GET    "/"                           request (list-employments ctx request))
    (GET    "/new"                        request (new-employment ctx request))
    (POST   "/new"                        request (create-employment ctx request))

    (context "/:employment-id" []
      (GET    "/edit"                     request (edit-employment ctx request))
      (PUT    "/edit"                     request (update-employment ctx request))
      (DELETE "/location-memberships"     request (delete-location-membership ctx request))
      (GET    "/location-memberships/new" request (new-location-membership ctx request))
      (POST   "/location-memberships"     request (create-location-membership ctx request)))))