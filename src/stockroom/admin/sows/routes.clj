(ns stockroom.admin.sows.routes
  (:require [compojure.core :refer [GET POST PUT DELETE context]]
            [stockroom.admin.sows.create-sow :refer [create-sow]]
            [stockroom.admin.sows.delete-sow :refer [delete-sow]]
            [stockroom.admin.sows.edit-sow :refer [edit-sow]]
            [stockroom.admin.sows.new-sow :refer [new-sow]]
            [stockroom.admin.sows.show-sow :refer [show-sow]]
            [stockroom.admin.sows.update-sow :refer [update-sow]]))

(defn handler [ctx]
  (context "/clients/:client-id/sows" []
           (GET "/new"           request (new-sow ctx request))
           (POST "/"             request (create-sow ctx request))
           (GET  "/:sow-id/edit" request (edit-sow ctx request))
           (PUT  "/:sow-id"      request (update-sow ctx request))
           (GET  "/:id"          request (show-sow ctx request))
           (DELETE "/:sow-id"    request (delete-sow ctx request))))