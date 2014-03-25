(ns stockroom.admin.users.routes
  (:require [compojure.core :refer [GET POST context]]
            [stockroom.admin.users.create-user :refer [create-user]]
            [stockroom.admin.users.list-users :refer [list-users]]
            [stockroom.admin.users.new-user :refer [new-user]]
            [stockroom.admin.users.show-user :refer [show-user]]))

(defn handler [ctx]
  (context "/users" []
    (GET  "/"     request (list-users ctx request))
    (POST "/"     request (create-user ctx request))
    (GET  "/new"  request (new-user ctx request))
    (GET  "/:id"  request (show-user ctx request))))
