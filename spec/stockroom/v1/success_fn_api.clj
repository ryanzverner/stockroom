(ns stockroom.v1.success-fn-api
  (:require [speclj.core :refer [stub]]
            [stockroom.v1.api :refer [V1Api]]
            [stockroom.v1.fn-api :refer [fn-api]]))

(defn success-fn-api [fns]
  (let [args {:invoke (fn [api & more] {:status :success :api api})}]
    (fn-api
      (merge
        (reduce
          (fn [acc method]
            (assoc acc method (stub method args)))
          {}
          (keys (:sigs V1Api)))
        fns))))
