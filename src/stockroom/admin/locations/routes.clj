(ns stockroom.admin.locations.routes
  (:require [compojure.core :refer [GET POST PUT routes]]
            [stockroom.admin.locations.create-location :refer [create-location]]
            [stockroom.admin.locations.list-locations :refer [list-locations]]
            [stockroom.admin.locations.new-location :refer [new-location]]))

(defn handler [ctx]
  (routes
    (GET  "/locations"                   request (list-locations ctx request))
    (GET  "/locations/new"               request (new-location ctx request))
    (POST "/locations/new"               request (create-location ctx request))))
