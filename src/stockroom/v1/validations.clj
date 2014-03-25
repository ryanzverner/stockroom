(ns stockroom.v1.validations
  (:require [stockroom.v1.api :refer :all]
            [stockroom.validation-machinery :refer [defvalidator validate only-if
                                                    unless return-errors alter-arguments-via]]
            [stockroom.v1.response :refer :all]))

(def valid-apprenticeship-skill-levels
  #{"student" "resident" "craftsman"})

(defn reset-position-id [data present? api]
  (let [position-from-name (find-employment-position-by-name api (data :position-name))]
    (if (= :success (position-from-name :status))
      [(assoc data :position-id (:id (position-from-name :result))) present? api]
      [data present? api])))

;; Predicates

(defn employment-position-matches? [data _ api]
  (let [position-from-name (find-employment-position-by-name api (:position-name data))]
    (not (and (= :success (:status position-from-name))
              (not (= (get (:result position-from-name) :id nil) (:position-id data)))))))

(defn position-name-exists-in-api? [data _ api]
  (= :success (:status (find-employment-position-by-name api (data :position-name)))))

(defn person-id-valid? [data _ api]
  (= :success (:status (find-person-by-id api (:person-id data)))))

(defn position-id-valid? [data _ api]
  (= :success (:status (find-employment-position-by-id api (:position-id data)))))

(defn employment-id-valid? [data _ api]
  (= :success (:status (find-employment-by-id api (:employment-id data)))))

(defn project-id-valid? [data _ api]
  (= :success (:status (find-project-by-id api (:project-id data)))))

(defn sow-id-valid? [data _ api]
  (= :success (:status (find-sow-by-id api (:sow-id data)))))

(defn skill-id-valid? [data _ api]
  (= :success (:status (find-skill-by-id api (:skill-id data)))))

(defn dates-in-chronological-order? [data _ _]
  (.before (:start data) (:end data)))

(defn dates-in-chronological-order-or-equal? [data _ _]
  (or (dates-in-chronological-order? data nil nil)
      (= (:start data) (:end data))))

(defn location-id-present? [data present? _]
  (present? data :location-id))

(defn location-id-valid? [data _ api]
  (= :success (:status (find-location-by-id api (:location-id data)))))

(defn location-name-present? [data present? _]
  (present? data :name))

(defn location-name-not-blank? [data _ _]
  (not= "" (:name data)))

(defn person-id-present? [data present? _]
  (present? data :person-id))

(defn position-id-present? [data present? _]
  (present? data :position-id))

(defn start-date-present? [data present? _]
  (present? data :start))

(defn end-date-present? [data _ _]
  (:end data))

(defn signed-date-present? [data present? _]
  (present? data :signed-date))

(defn position-name-present? [data _ _]
  (:position-name data))

(defn position-name-valid? [data present? api]
  (not (and (position-name-present? data present? api)
            (not (position-name-exists-in-api? data present? api)))))

(defn employment-id-present? [data present? _]
  (present? data :employment-id))

(defn project-id-present? [data present? _]
  (present? data :project-id))

(defn sow-id-present? [data present? _]
  (present? data :sow-id))

(defn skill-id-present? [data present? _]
  (present? data :skill-id))

(defn apprenticeship-skill-level-present? [data _ _]
  (:skill-level data))

(defn mentorships-present? [data _ _]
  (seq (:mentorships data)))

(defn all-mentors-present? [data _ _]
  (every? #(:person-id %) (:mentorships data)))

(defn all-mentorship-start-dates-present? [data present? api]
  (every? #(start-date-present? % present? api)
          (:mentorships data)))

(defn all-mentorship-end-dates-present? [data present? api]
  (every? #(end-date-present? % present? api)
          (:mentorships data)))

(defn all-mentorship-dates-in-chronological-order? [data present? api]
  (every? #(dates-in-chronological-order? % present? api) (:mentorships data)))

(defn apprenticeship-skill-level-valid? [data _ _]
  (valid-apprenticeship-skill-levels (:skill-level data)))

(defn mentorship-dates-range-within-apprenticeships? [data present? api]
  (let [a-start (:start data)
        a-end (:end data)]
    (every? #(and (or (.before a-start (:start %))
                      (.equals a-start (:start %)))
                  (or (.after a-end (:end %))
                      (.equals a-end (:end %))))
            (:mentorships data))))

(defn mysql-api? [_ _ api]
  (= (str (type api))
     "class stockroom.v1.mysql_api.V1MysqlApi"))

(defn correct-position-values-present? [data present? api]
  (or (position-id-present? data present? api)
      (and (position-name-present? data present? api)
           (not (position-name-exists-in-api? data present? api)))))

;; Employment-specific validation machinery

(defn return-data-and-errors [[data present? api]]
  (fn [errors]
    [data errors]))

;; Validations

(defvalidator validate-apprenticeship [apprenticeship-data present? api]
  (validate person-id-present?
            apprenticeships-missing-person-id)

  (validate apprenticeship-skill-level-present?
            apprenticeships-missing-skill-level)

  (validate start-date-present?
            apprenticeships-missing-start-date)

  (validate end-date-present?
            apprenticeships-missing-end-date)

  (validate mentorships-present?
            apprenticeships-missing-mentorships)

  (only-if [start-date-present? end-date-present?]
    (validate dates-in-chronological-order?
              apprenticeships-invalid-date-range))

  (only-if [apprenticeship-skill-level-present?]
    (validate apprenticeship-skill-level-valid?
              apprenticeships-invalid-skill-level))

  (only-if [person-id-present?]
    (validate person-id-valid?
              apprenticeships-invalid-person-id))

  (only-if [mentorships-present?]
    (validate all-mentors-present?
              apprenticeships-missing-mentor)

    (validate all-mentorship-start-dates-present?
              apprenticeships-missing-mentorship-start-date)

    (validate all-mentorship-end-dates-present?
              apprenticeships-missing-mentorship-end-date)

    (only-if [all-mentorship-start-dates-present? all-mentorship-end-dates-present?]
      (validate all-mentorship-dates-in-chronological-order?
                apprenticeships-invalid-mentorship-date-range)

        (only-if [start-date-present? end-date-present? dates-in-chronological-order?]
          (validate mentorship-dates-range-within-apprenticeships?
                    apprenticeships-invalid-mentorship-date-range))))

  return-errors)

(defvalidator validate-project-sow [project-sow-data present? api]
  (validate project-id-present?
            project-sows-missing-project-id)

  (validate sow-id-present?
            project-sows-missing-sow-id)

  (unless [mysql-api?]
    (only-if [project-id-present?]
      (validate project-id-valid?
                project-sows-invalid-project-id))

    (only-if [sow-id-present?]
      (validate sow-id-valid?
                project-sows-invalid-sow-id)))

  return-errors)

(defvalidator validate-project-skill [project-skill-data present? api]
  (validate project-id-present?
            project-skills-missing-project-id)

  (validate skill-id-present?
            project-skills-missing-skill-id)

  (unless [mysql-api?]
    (only-if [project-id-present?]
      (validate project-id-valid?
                project-skills-invalid-project-id))

    (only-if [skill-id-present?]
      (validate skill-id-valid?
                project-skills-invalid-skill-id)))

  return-errors)

(defvalidator validate-engagement [engagement-data present? api]
  (validate start-date-present?
            engagements-missing-start-date)

  (validate end-date-present?
            engagements-missing-end-date)

  (validate employment-id-present?
            engagements-missing-employment-id)

  (validate project-id-present?
            engagements-missing-project-id)

  (unless [mysql-api?]
    (only-if [employment-id-present?]
      (validate employment-id-valid?
                engagements-invalid-employment-id))

    (only-if [project-id-present?]
      (validate project-id-valid?
                engagements-invalid-project-id)))

  return-errors)

(defvalidator validate-employment [employment-data present? api]
  (validate person-id-present?
            employment-missing-person-id)

  (validate start-date-present?
            employment-missing-start-date)

  (only-if [position-name-present? position-id-present?]
    (validate employment-position-matches?
              employment-position-mismatch))

  (validate position-name-valid?
            employment-invalid-position-name)

  (alter-arguments-via reset-position-id)

  (validate correct-position-values-present?
            employment-missing-position-id)

  (unless [mysql-api?]
    (only-if [person-id-present?]
      (validate person-id-valid?
                employment-invalid-person-id))

    (only-if [position-id-present?]
      (validate position-id-valid?
                employment-invalid-position-id)))

  return-data-and-errors)

(defvalidator validate-director-engagement [director-engagement-data present? api]
  (validate person-id-present?
            director-engagements-missing-person-id)

  (validate project-id-present?
            director-engagements-missing-project-id)

  (validate start-date-present?
            director-engagements-missing-start-date)

  (only-if [start-date-present? end-date-present?]
    (validate dates-in-chronological-order-or-equal?
              director-engagements-invalid-date-range))

  return-errors)

(defvalidator validate-location [location-data present? api]
  (validate location-name-present?
            locations-missing-name)

  (validate location-name-not-blank?
            locations-missing-name)

  return-errors)

(defvalidator validate-location-membership [location-membership-data present? api]
  (validate employment-id-present?
            location-memberships-missing-employment-id)

  (validate location-id-present?
            location-memberships-missing-location-id)

  (validate start-date-present?
            location-membership-missing-start-date)

  return-errors)

(defvalidator validate-employment-with-location [employment-with-location-data present? api]
  (validate person-id-present?
            employment-missing-person-id)

  (validate start-date-present?
            employment-missing-start-date)

  (only-if [position-name-present? position-id-present?]
    (validate employment-position-matches?
              employment-position-mismatch))

  (validate position-name-valid?
            employment-invalid-position-name)

  (alter-arguments-via reset-position-id)

  (validate correct-position-values-present?
            employment-missing-position-id)

  (validate location-id-present?
            location-memberships-missing-location-id)

  (unless [mysql-api?]
    (only-if [person-id-present?]
      (validate person-id-valid?
                employment-invalid-person-id))

    (only-if [position-id-present?]
      (validate position-id-valid?
                employment-invalid-position-id))

    (only-if [location-id-present?]
      (validate location-id-valid?
                location-memberships-invalid-location-id)))

  return-data-and-errors)
