(ns stockroom.middleware.accept
  (:require [ring.middleware.format-response :refer [parse-accept-header]]))

(defn wrap-accept [handler fallback-handler type sub-type]
  (fn [request]
    (let [acceptable (parse-accept-header (get-in request [:headers "accept"] (:content-type request)))
          matches? (some #(and (= type (:type %)) (= sub-type (:sub-type %))) acceptable)]
      (if matches?
        (handler request)
        (fallback-handler request)))))
