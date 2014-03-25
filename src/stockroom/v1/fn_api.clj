(ns stockroom.v1.fn-api
  (:require [stockroom.v1.api :refer [V1Api]]))

(defmacro deffnapi []
  (let [methods (reduce
                  (fn [methods [method-name sig]]
                    (reduce
                      (fn [methods arglist]
                        (conj methods
                          `(~(:name sig) ~arglist
                              ((~method-name ~'fns) ~@arglist))))
                      methods
                      (:arglists sig)))
                    []
                    (:sigs V1Api))]
    (list* 'deftype 'FnApi ['fns] 'V1Api methods)))

(deffnapi)

(defn fn-api [fns]
  (FnApi. fns))
