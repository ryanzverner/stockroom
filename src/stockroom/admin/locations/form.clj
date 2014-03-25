(ns stockroom.admin.locations.form
  (:require [metis.core :refer [defvalidator]]
            [metis.util :refer [present?]]))

(def missing-name-error "Please enter a location name.")

(defn form-params->location [params]
  (select-keys params [:name]))

(defvalidator validate-location-form
  [:name :presence {:message missing-name-error}])
