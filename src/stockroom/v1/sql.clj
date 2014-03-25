(ns stockroom.v1.sql)

(def upcoming-apprentice-graduations-by-location-sql
  "select people.first_name as `first-name`,
          people.last_name as `last-name`,
          locations.name as `location-name`,
          apprenticeships.end as `graduates-at`
   from employment
   left join people on people.id = employment.person_id
   left join location_memberships on location_memberships.employment_id = employment.id
   left join locations on location_memberships.location_id = locations.id
   left join apprenticeships on apprenticeships.person_id = people.id
   where location_memberships.start = (select r.start
                              from location_memberships as r
                              where r.employment_id = employment.id
                              order by r.start desc limit 1)
   and apprenticeships.end > now();")

(defn find-apprenticeships-sql
  ([] (find-apprenticeships-sql :not-where))

  ([where]
    (format "select apprenticeships.id as apprenticeship_id,
                    apprentices.id as apprentice_id,
                    apprentices.first_name as apprentice_first_name,
                    apprentices.last_name as apprentice_last_name,
                    apprentices.email as apprentice_email,
                    apprentices.created_at as apprentice_created_at,
                    apprentices.updated_at as apprentice_updated_at,

                    apprenticeships.skill_level as apprenticeship_skill_level,
                    apprenticeships.start as apprenticeship_start,
                    apprenticeships.end as apprenticeship_end,
                    apprenticeships.created_at as apprenticeship_created_at,
                    apprenticeships.updated_at as apprenticeship_updated_at,

                    mentors.id as mentor_id,
                    mentors.first_name as mentor_first_name,
                    mentors.last_name as mentor_last_name,
                    mentors.email as mentor_email,
                    mentors.created_at as mentor_created_at,
                    mentors.updated_at as mentor_updated_at,

                    mentorships.id as mentorship_id,
                    mentorships.start as mentorship_start,
                    mentorships.end as mentorship_end,
                    mentorships.created_at as mentorship_created_at,
                    mentorships.updated_at as mentorship_updated_at

             from mentorships

             left join apprenticeships on apprenticeships.person_id = mentorships.apprentice_id
             left join people as mentors on mentors.id = mentorships.mentor_id
             left join people as apprentices on apprenticeships.person_id = apprentices.id %s"

          (if (= :where where) "where apprenticeships.id = ?;" ""))))

(defn find-employment-sql []
     "select employment.id as employment_id,
                    employment.start as employment_start,
                    employment.end as employment_end,
                    employment.position_id as employment_position_id,
                    employment.person_id as employment_position_id,
                    employment.created_at as employment_created_at,
                    employment.updated_at as employment_updated_at,

                    people.id as person_id,
                    people.first_name as person_first_name,
                    people.last_name as person_last_name,
                    people.email as person_email,
                    people.created_at as person_created_at,
                    people.updated_at as person_updated_at,

                    positions.id as position_id,
                    positions.name as position_name,
                    positions.created_at as position_created_at,
                    positions.updated_at as position_updated_at

             from employment

             left join positions on employment.position_id = positions.id
             left join people on employment.person_id = people.id

            where employment.id = ?;")
