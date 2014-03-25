(ns stockroom.v1.response)

(def duplicate-authentication
  {:code :authentication/duplicate
   :description "The uid/provider pair must be unqiue."})

(def duplicate-group-name
  {:code :group/duplicate
   :description "The group name must be unique."})

(def employment-missing-person-id
  {:code :employment/missing-person-id
   :description "A person id is required when creating an employment record."})

(def employment-missing-position-id
  {:code :employment/missing-position-id
   :description "A position id is required when creating an employment record."})

(def employment-missing-start-date
  {:code :employment/missing-start-date
   :description "A start date is required when creating an employment record."})

(def employment-invalid-person-id
  {:code :employment/invalid-person-id
   :description "A valid person id is required when creating an employment record."})

(def employment-invalid-position-id
  {:code :employment/invalid-position-id
   :description "A valid position id is required when creating an employment record."})

(def employment-invalid-position-name
  {:code :employment/invalid-position-name
   :description "If you send a position name when creating an employment record, it must be valid. You may also send a position id instead."})

(def employment-position-mismatch
  {:code :employment/position-data-mismatch
   :description "If you send a position name and a position id when creating an employment record, they must refer to the same position record."})

(def employment-data-missing-location-id
  {:code :location-memberships/missing-location-id
   :description "A location ID is required when creating a location-membership"})

(def employment-data-invalid-location-id
  {:code :location-memberships/location-not-found
   :description "No location corresponds to the provided location ID"})

(def sows-missing-start-date
  {:code :sows/missing-start-date
   :description "A start date is required when creating a SOW record."})

(def sows-missing-signed-date
  {:code :sows/sow-missing-signed-date
   :description "A signed date is required when creating a SOW that has been signed."})

(def project-sows-missing-project-id
  {:code :project-sows/missing-project-id
   :description "A project id is required when creating a project SOW record."})

(def project-sows-invalid-project-id
  {:code :project-sows/invalid-project-id
   :description "A valid project id is required when creating a project SOW."})

(def project-sows-missing-sow-id
  {:code :project-sows/missing-sow-id
   :description "A SOW id is required when creating a project SOW record."})

(def project-sows-invalid-sow-id
  {:code :project-sows/invalid-sow-id
   :description "A valid SOW id is required when creating a project SOW."})

(def project-skills-missing-project-id
  {:code :project-skills/missing-project-id
   :description "A project id is required when creating a project skill record."})

(def project-skills-invalid-project-id
  {:code :project-skills/invalid-project-id
   :description "A valid project id is required when creating a project skill."})

(def project-skills-missing-skill-id
  {:code :project-skills/missing-skill-id
   :description "A skill id is required when creating a project skill record."})

(def project-skills-invalid-skill-id
  {:code :project-skills/invalid-skill-id
   :description "A valid skill id is required when creating a project skill."})

(def engagements-missing-employment-id
  {:code :engagements/missing-employment-id
   :description "An employment id is required when creating a staffing engagement."})

(def engagements-invalid-employment-id
  {:code :engagements/invalid-employment-id
   :description "A valid employment id is required when creating a staffing engagement."})

(def engagements-missing-project-id
  {:code :engagements/missing-project-id
   :description "A project id is required when creating a staffing engagement."})

(def engagements-invalid-project-id
  {:code :engagements/invalid-project-id
   :description "A valid project id is required when creating a staffing engagement."})

(def engagements-missing-start-date
  {:code :engagements/missing-start-date
   :description "A start date is required when creating an engagement."})

(def engagements-missing-end-date
  {:code :engagements/missing-end-date
   :description "An end date is required when creating an engagement."})

(def apprenticeships-missing-person-id
  {:code :apprenticeships/missing-person-id
   :description "A person id is required when creating an apprenticeship record"})

(def apprenticeships-invalid-person-id
  {:code :apprenticeships/invalid-person-id
   :description "A valid person id is required when creating an apprenticeship record"})

(def apprenticeships-missing-skill-level
  {:code :apprenticeships/missing-skill-level
   :description "A skill level is required when creating an apprenticeship record"})

(def apprenticeships-invalid-skill-level
  {:code :apprenticeships/invalid-skill-level
   :description "A valid skill level is required when creating an apprenticeship record"})

(def apprenticeships-missing-start-date
  {:code :apprenticeships/missing-start-date
   :description "A start date is required when creating an apprenticeship record"})

(def apprenticeships-missing-end-date
  {:code :apprenticeships/missing-end-date
   :description "An end date is required when creating an apprenticeship record"})

(def apprenticeships-invalid-date-range
  {:code :apprenticeships/invalid-date-range
   :description "A valid date range is required when creating an apprenticeship record"})

(def apprenticeships-missing-mentorships
  {:code :apprenticeships/missing-mentorships
   :description "An apprenticeship requires at least one associated mentorship"})

(def apprenticeships-missing-mentor
  {:code :apprenticeships/missing-mentor
   :description "An apprenticeship requires all associated mentorships to have a mentor"})

(def apprenticeships-missing-mentorship-start-date
  {:code :apprenticeships/missing-mentorship-start-date
   :description "An apprenticeship requires all associated mentorships to have a start date"})

(def apprenticeships-missing-mentorship-end-date
  {:code :apprenticeships/missing-mentorship-end-date
   :description "An apprenticeship requires all associated mentorships to have an end date"})

(def apprenticeships-invalid-mentorship-date-range
  {:code :apprenticeships/invalid-mentorship-date-range
   :description "An apprenticeship requires all associated mentorships to have valid date ranges"})

(def director-engagements-missing-person-id
  {:code :director-engagements/missing-person-id
   :description "A person id is required when creating a director-engagement record"})

(def director-engagements-invalid-person-id
  {:code :director-engagements/invalid-person-id
   :description "A valid person id is required when creating a director-engagement record"})

(def director-engagements-missing-project-id
  {:code :director-engagements/missing-project-id
   :description "A project id is required when creating a director-engagement record"})

(def director-engagements-invalid-project-id
  {:code :director-engagements/invalid-project-id
   :description "A valid project id is required when creating a director-engagement record"})

(def director-engagements-missing-start-date
  {:code :director-engagements/missing-start-date
   :description "A start date is required when creating a director-engagement"})

(def director-engagements-invalid-date-range
  {:code :director-engagements/invalid-date-range
   :description "A valid start and end date range is required when creating a director-engagement record"})

(def locations-missing-name
  {:code :locations/missing-name
   :description "A name is required when creating a location"})

(def locations-duplicate-name
  {:code :locations/duplicate-name
   :description "A location's name must be unique"})

(def location-memberships-missing-employment-id
  {:code :location-memberships/missing-employment-id
   :description "An employment ID is required when creating a location-membership"})

(def location-memberships-employment-not-found
  {:code :location-memberships/employment-not-found
   :description "No employment corresponds to the provided person ID"})

(def location-memberships-missing-location-id
  {:code :location-memberships/missing-location-id
   :description "A location ID is required when creating a location-membership"})

(def location-memberships-invalid-location-id
  {:code :location-memberships/invalid-location-id
   :description "A valid location ID is required when creating a location-membership"})

(def location-memberships-location-not-found
  {:code :location-memberships/location-not-found
   :description "No location corresponds to the provided location ID"})

(def location-membership-missing-start-date
  {:code :location-memberships/missing-start-date
    :description "A start date is required when creating a location-membership"})

(defn success [api result]
  {:api api :result result :status :success})

(defn not-found [api]
  {:api api :status :not-found})

(defn failure [api errors]
  {:api api :status :failure :errors errors})
