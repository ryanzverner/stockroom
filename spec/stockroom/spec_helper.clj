(ns stockroom.spec-helper
  (:require [chee.datetime :as time]
            [speclj.core :refer [-fail]]
            [stockroom.config :as config]
            [stockroom.v1.memory-api :as v1-memory]
            [stockroom.v1.mysql-api :as v1-mysql]))

(defmacro do-at [date & body]
  `(with-redefs [time/now (fn [] ~date)]
                ~@body))

(def test-db-spec (delay (:test (config/read-config "database.clj"))))

(defmacro should-implement [protocol type]
  `(let [munged-protocol-method-names# (map #(munge (name %)) (keys (:sigs ~protocol)))
         munged-type-names# (map #(.getName %) (.getDeclaredMethods ~type))]
     (doseq [protocol-method-name# munged-protocol-method-names#]
       (when-not (some #(= % protocol-method-name#) munged-type-names#)
         (-fail (format "Expected %s to implement %s but did not" ~type protocol-method-name#))))))

(defn v1-mysql-api []
  (v1-mysql/mysql-api @test-db-spec))

(defn v1-memory-api []
  (v1-memory/memory-api))
