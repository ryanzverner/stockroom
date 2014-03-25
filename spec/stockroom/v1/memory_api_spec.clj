(ns stockroom.v1.memory-api-spec
  (:require [speclj.core :refer :all]
            [stockroom.spec-helper :refer [v1-memory-api]]
            [stockroom.v1.api-spec :refer [api-spec]]))

(describe "stockroom.v1.memory-api"
  (api-spec (fn [] (v1-memory-api))))
