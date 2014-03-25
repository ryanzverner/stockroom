(ns stockroom.admin.apprenticeships.routes
  (:require [compojure.core :refer [GET POST PUT context]]
            [stockroom.admin.apprenticeships.list-apprenticeships :refer [list-apprenticeships]]
            [stockroom.admin.apprenticeships.new-apprenticeship :refer [new-apprenticeship]]
            [stockroom.admin.apprenticeships.create-apprenticeship :refer [create-apprenticeship]]))

(defn handler [ctx]
  (context "/apprenticeships" []
    (GET  "/"    request (list-apprenticeships ctx request))
    (GET  "/new" request (new-apprenticeship ctx request))
    (POST "/"    request (create-apprenticeship ctx request))))
