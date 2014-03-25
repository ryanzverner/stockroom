(ns stockroom.admin.people.form
  (:require [metis.core :refer [defvalidator]]
            [metis.util :refer [present?]]))

(def missing-first-name-error "Please enter a first name.")
(def missing-last-name-error "Please enter a last name.")
(def missing-email-error "Please enter an email.")
(def invalid-email-error "Please enter a valid email.")

(defn form-params->person [params]
  (select-keys params [:first-name :last-name :email]))

(defvalidator validate-person-form
  [:first-name :presence {:message missing-first-name-error}]
  [:last-name :presence {:message missing-last-name-error}]
  [:email [:presence {:message missing-email-error}
           :email {:message invalid-email-error
                   :if #(present? (:email %))}]]
              )
