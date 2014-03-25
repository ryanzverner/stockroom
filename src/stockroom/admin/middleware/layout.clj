(ns stockroom.admin.middleware.layout
  (:require [stockroom.admin.layout :refer [render-layout]]))

(defn wrap-layout [handler context]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (render-layout (:body response) context)))))
