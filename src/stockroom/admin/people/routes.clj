(ns stockroom.admin.people.routes
  (:require [compojure.core :refer [GET POST PUT routes]]
            [stockroom.admin.people.create-person :refer [create-person]]
            [stockroom.admin.people.edit-person :refer [edit-person]]
            [stockroom.admin.people.list-people :refer [list-people]]
            [stockroom.admin.people.new-person :refer [new-person]]
            [stockroom.admin.people.update-person :refer [update-person]]))

(defn handler [ctx]
  (routes
    (GET  "/people"                 request (list-people ctx request))
    (GET  "/people/new"             request (new-person ctx request))
    (POST "/people/new"             request (create-person ctx request))
    (GET  "/people/:person-id/edit" request (edit-person ctx request))
    (PUT  "/people/:person-id/edit" request (update-person ctx request))
    ))
