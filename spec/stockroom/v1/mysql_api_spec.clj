(ns stockroom.v1.mysql-api-spec
  (:require [korma.db :as kdb]
            [speclj.core :refer :all]
            [stockroom.spec-helper :refer [v1-mysql-api]]
            [stockroom.v1.api-spec :refer [api-spec]]
            [stockroom.v1.mysql-api :as mysql]))

(defmacro with-rollback [api & body]
  `(mysql/with-db ~api
     (kdb/transaction
       (kdb/rollback)
       (try
        ~@body
        (catch Exception e#
          (throw e#))
        (catch Throwable e#
          (throw (Exception. "Caught throwable: " e#)))))))

(describe "stockroom.v1.mysql-api"
  (let [api (v1-mysql-api)]
    (list*
      (around [it] (with-rollback api (it)))
      (api-spec (fn [] api)))))
