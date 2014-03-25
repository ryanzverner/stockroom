(ns stockroom.admin.projects.routes
  (:require [compojure.core :refer [GET POST PUT DELETE context]]
            [stockroom.admin.projects.create-project :refer [create-project]]
            [stockroom.admin.projects.edit-project :refer [edit-project]]
            [stockroom.admin.projects.new-project :refer [new-project]]
            [stockroom.admin.projects.update-project :refer [update-project]]
            [stockroom.admin.projects.delete-project :refer [delete-project]]))

(defn handler [ctx]
  (context "/clients/:client-id/projects" []
           (GET "/new"               request (new-project ctx request))
           (POST "/"                 request (create-project ctx request))
           (GET  "/:project-id/edit" request (edit-project ctx request))
           (PUT  "/:project-id"      request (update-project ctx request))
           (DELETE "/:project-id"    request (delete-project ctx request))))
