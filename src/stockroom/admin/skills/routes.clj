(ns stockroom.admin.skills.routes
  (:require [compojure.core :refer [GET POST PUT context]]
            [stockroom.admin.skills.create-skill :refer [create-skill]]
            [stockroom.admin.skills.edit-skill :refer [edit-skill]]
            [stockroom.admin.skills.list-skills :refer [list-skills]]
            [stockroom.admin.skills.new-skill :refer [new-skill]]
            [stockroom.admin.skills.show-skill :refer [show-skill]]
            [stockroom.admin.skills.update-skill :refer [update-skill]]))

(defn handler [ctx]
  (context "/skills" []
    (GET  "/"         request (list-skills ctx request))
    (POST "/"         request (create-skill ctx request))
    (GET  "/new"      request (new-skill ctx request))
    (GET  "/:id"      request (show-skill ctx request))
    (GET  "/:id/edit" request (edit-skill ctx request))
    (PUT  "/:id"      request (update-skill ctx request))))
