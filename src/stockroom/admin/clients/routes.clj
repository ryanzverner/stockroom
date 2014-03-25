(ns stockroom.admin.clients.routes
  (:require [compojure.core :refer [GET POST PUT DELETE context]]
            [stockroom.admin.clients.create-client :refer [create-client]]
            [stockroom.admin.clients.edit-client :refer [edit-client]]
            [stockroom.admin.clients.list-clients :refer [list-clients]]
            [stockroom.admin.clients.new-client :refer [new-client]]
            [stockroom.admin.clients.show-client :refer [show-client]]
            [stockroom.admin.clients.update-client :refer [update-client]]
            [stockroom.admin.clients.delete-client :refer [delete-client]]))

(defn handler [ctx]
  (context "/clients" []
    (GET  "/"         request (list-clients ctx request))
    (POST "/"         request (create-client ctx request))
    (GET  "/new"      request (new-client ctx request))
    (GET  "/:id"      request (show-client ctx request))
    (GET  "/:id/edit" request (edit-client ctx request))
    (PUT  "/:id"      request (update-client ctx request))
    (DELETE "/:id"    request (delete-client ctx request))))
