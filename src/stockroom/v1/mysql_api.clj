(ns stockroom.v1.mysql-api
  (:require [chee.datetime :refer [now before? after? days before]]
            [clojure.java.jdbc.deprecated :as jdbc]
            [clojure.string :as string]
            [korma.core :as k]
            [korma.db :as kdb]
            [korma.sql.engine :as engine]
            [korma.sql.utils :as utils]
            [stockroom.util.time :refer [at-midnight from-date-string days-ago-at-midnight]]
            [stockroom.v1.api :refer [V1Api find-employment-position-by-name]]
            [stockroom.v1.validations :refer [validate-apprenticeship
                                              validate-engagement
                                              validate-project-sow
                                              validate-project-skill
                                              validate-employment
                                              validate-director-engagement
                                              validate-location
                                              validate-location-membership
                                              validate-employment-with-location]]
            [stockroom.v1.sql :refer [find-apprenticeships-sql
                                      find-employment-sql
                                      upcoming-apprentice-graduations-by-location-sql]]
            [stockroom.v1.response :refer :all]))

(defmacro with-null-out-str [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       ~@body)))

(defn update-data [data mapping]
  (reduce
    (fn [acc [domain-key db-key]]
      (if (contains? data domain-key)
        (assoc acc db-key (domain-key data))
        acc))
    {}
    mapping))

(defn insert-data [data mapping]
  (reduce
    (fn [acc [domain-key db-key]]
      (assoc acc db-key (domain-key data)))
    {}
    mapping))

(defn data-from-db [row mapping]
  (reduce
    (fn [acc [domain-key db-key]]
      (assoc acc domain-key (db-key row)))
    {}
    mapping))

(defn with-create-timestamps [data]
  (let [now (now)]
    (assoc data
           :created_at now
           :updated_at now)))

(defn with-update-timestamps [data]
  (let [now (now)]
    (-> data
      (assoc :updated_at now)
      (dissoc :created_at))))

(defn with-insert-data [data mapping]
  (-> data
    (insert-data mapping)
    with-create-timestamps))

(defn with-update-data [data db-mapping]
  (-> data
    (update-data db-mapping)
    (dissoc :id)
    with-update-timestamps))

(defn insert! [entity row mapping]
  (let [insert-data (with-insert-data row mapping)]
    (with-null-out-str
      (:GENERATED_KEY
        (k/insert entity (k/values insert-data))))))

(defn update! [entity row-id mapping row]
  (let [update-data (with-update-data row mapping)
        {:keys [sql-str params]} (k/query-only
                                   (k/update entity
                                             (k/set-fields update-data)
                                             (k/where {:id [= row-id]})))]
    (with-null-out-str
      (first (k/exec-raw (kdb/get-connection (:db entity)) [sql-str params])))))

(defn delete-by-id [entity row-id]
  (let [{:keys [sql-str params]}
        (k/query-only
          (k/delete entity (k/where {:id [= row-id]})))]
    (first (k/exec-raw (kdb/get-connection (:db entity)) [sql-str params]))))

(defn delete-where [entity column value]
  (let [{:keys [sql-str params]}
        (k/query-only
          (k/delete entity
            (k/where {column [= value]})))]
    (first (k/exec-raw (kdb/get-connection (:db entity)) [sql-str params]))))

(defn invalid-int-error? [message]
  (.startsWith message "Incorrect integer value:"))

(defn present? [data k]
  (k data))

(defn field-identifier [table field]
  (utils/pred
    #(string/join "." [(engine/field-identifier table)
                       (engine/field-identifier field)])
    []))

(defn entity [db table]
  (-> (k/create-entity table)
    (k/database db)))


(defn client-entity [db] (entity db "clients"))
(defn skill-entity [db] (entity db "skills"))
(defn project-entity [db] (entity db "projects"))
(defn sow-entity [db] (entity db "sows"))
(defn project-sow-entity [db] (entity db "project_sows"))
(defn project-skill-entity [db] (entity db "project_skills"))
(defn users-entity [db] (entity db "users"))
(defn authentication-entity [db] (entity db "authentications"))
(defn group-entity [db] (entity db "groups"))
(defn group-permission-entity [db] (entity db "group_permissions"))
(defn group-user-entity [db] (entity db "group_users"))
(defn person-entity [db] (entity db "people"))
(defn position-entity [db] (entity db "positions"))
(defn employment-entity [db] (entity db "employment"))
(defn engagement-entity [db] (entity db "engagements"))
(defn apprenticeship-entity [db] (entity db "apprenticeships"))
(defn mentorship-entity [db] (entity db "mentorships"))
(defn director-engagement-entity [db] (entity db "director_engagements"))
(defn location-entity [db] (entity db "locations"))
(defn location-membership-entity [db] (entity db "location_memberships"))

(defn associate-mentorship-to-apprenticeship [mentorship-data id apprenticeship-data]
  (merge mentorship-data {:apprenticeship-id id
                          :apprentice-id (:person-id apprenticeship-data)}))

(defn make-location-membership-data-from-employment-data [employment-data employment-id]
  (assoc {} :employment-id employment-id
            :location-id (:location-id employment-data)
            :start (:start employment-data)))

(def client-mapping
  {:id         :id
   :name       :name
   :created-at :created_at
   :updated-at :updated_at})

(defn- row->client [row]
  (data-from-db row client-mapping))

(def skill-mapping
  {:id         :id
   :name       :name
   :created-at :created_at
   :updated-at :updated_at})

(defn- row->skill [row]
  (data-from-db row skill-mapping))

(def project-mapping
  {:id         :id
   :name       :name
   :source-url :source_url
   :client-id  :client_id
   :created-at :created_at
   :updated-at :updated_at})

(defn- row->project [row]
  (data-from-db row project-mapping))

(def sow-mapping
  {:id            :id
   :hourly-rate   :hourly_rate
   :currency-code :currency_code
   :start         :start
   :end           :end
   :url           :url
   :signed-date   :signed_date
   :created-at    :created_at
   :updated-at    :updated_at})

(defn- row->sow [row]
  (data-from-db row sow-mapping))

(def project-sow-mapping
  {:id          :id
   :project-id  :project_id
   :sow-id      :sow_id
   :created-at  :created_at
   :updated-at  :updated_at})

(def project-skill-mapping
  {:id          :id
   :project-id  :project_id
   :skill-id    :skill_id
   :created-at  :created_at
   :updated-at  :updated_at})

(defn- row->project-sow [row]
  (data-from-db row project-sow-mapping))

(defn- row->project-skill [row]
  (data-from-db row project-skill-mapping))

(def authentication-mapping
  {:created-at :created_at
   :provider   :provider
   :uid        :uid
   :updated-at :updated_at
   :user-id    :user_id})

(defn- authentication->row [p]
  (assoc p :provider (name (:provider p))))

(defn- row->authentication [row]
  (let [data (data-from-db row authentication-mapping)]
    (assoc data :provider (keyword (:provider data)))))

(def user-mapping
  {:created-at :created_at
   :id         :id
   :name       :name
   :updated-at :updated_at})

(defn- row->user [r]
  (data-from-db r user-mapping))

(def group-mapping
  {:created-at :created_at
   :id         :id
   :name       :name
   :updated-at :updated_at})

(defn row->group [r]
  (data-from-db r group-mapping))

(def group-permission-mapping
  {:created-at :created_at
   :group-id   :group_id
   :permission :permission
   :updated-at :updated_at})

(def group-user-mapping
  {:created-at :created_at
   :group-id   :group_id
   :updated-at :updated_at
   :user-id    :user_id})

(def person-mapping
  {:created-at :created_at
   :email      :email
   :first-name :first_name
   :id         :id
   :last-name  :last_name
   :updated-at :updated_at})

(defn- row->person [row]
  (data-from-db row person-mapping))

(def position-mapping
  {:created-at :created_at
   :id         :id
   :name       :name
   :updated-at :updated_at})

(defn- row->position [r]
  (data-from-db r position-mapping))

(def employment-mapping
  {:id          :id
   :end         :end
   :person-id   :person_id
   :position-id :position_id
   :start       :start
   :created-at  :created_at
   :updated-at  :updated_at})

(defn- row->employment [row]
  (data-from-db row employment-mapping))

(def engagement-mapping
  {:id                      :id
   :end                     :end
   :start                   :start
   :confidence-percentage   :confidence_percentage
   :employment-id           :employment_id
   :project-id              :project_id
   :created-at              :created_at
   :updated-at              :updated_at})

(def apprenticeship-mapping
  {:id          :id
   :person-id   :person_id
   :skill-level :skill_level
   :start       :start
   :end         :end
   :created-at  :created_at
   :updated-at  :updated_at})

(def mentorship-mapping
  {:id                :id
   :apprentice-id     :apprentice_id
   :person-id         :mentor_id
   :start             :start
   :end               :end
   :created-at        :created_at
   :updated-at        :updated_at})

(def director-engagement-mapping
  {:id            :id
   :project-id    :project_id
   :person-id     :person_id
   :start         :start
   :end           :end
   :created-at    :created_at
   :updated-at    :updated_at})

(def location-mapping
  {:id         :id
   :name       :name
   :created-at :created_at
   :updated-at :updated_at})

(defn row->location [row]
  (data-from-db row location-mapping))

(defn- row->director-engagement [row]
  (data-from-db row director-engagement-mapping))

(defn convert-from-sql-date [data key]
  (update-in data [key] #(if % (at-midnight %) %)))

(defn assign-default-confidence-pct-if-missing [engagement-data]
  (if (nil? (:confidence-percentage engagement-data))
    (assoc engagement-data :confidence-percentage 100)
    engagement-data))

(defn make-mentorship [row]
  {:person-id (:mentor_id row)
   :person {:id (:mentor_id row)
            :first-name (:mentor_first_name row)
            :last-name (:mentor_last_name row)
            :email (:mentor_email row)
            :created-at (:mentor_created_at row)
            :updated-at (:mentor_updated_at row)}
   :apprentice-id (:apprentice_id row)
   :apprenticeship-id (:apprenticeship_id row)
   :start (:mentorship_start row)
   :end (:mentorship_end row)
   :created-at (:mentorship_created_at row)
   :updated-at (:mentorship_updated_at row)
   :id (:mentorship_id row)})

(defn make-apprenticeship [row]
  {:id (:apprenticeship_id row)
   :person-id (:apprentice_id row)
   :person {:created-at (:apprentice_created_at row)
            :id (:apprentice_id row)
            :first-name (:apprentice_first_name row)
            :last-name (:apprentice_last_name row)
            :email (:apprentice_email row)
            :updated-at (:apprentice_updated_at row)}
   :skill-level (:apprenticeship_skill_level row)
   :start (:apprenticeship_start row)
   :end (:apprenticeship_end row)
   :created-at (:apprenticeship_created_at row)
   :updated-at (:apprenticeship_updated_at row)
   :mentorships [(make-mentorship row)]})

(defn make-employment [row]
  {:id (:employment_id row)
   :person-id (:person_id row)
   :person {:created-at (:person_created_at row)
            :id (:person_id row)
            :first-name (:person_first_name row)
            :last-name (:person_last_name row)
            :email (:person_email row)
            :updated-at (:person_updated_at row)}
   :position-id (:employment_position_id row)
   :position {:created-at (:position_created_at row)
              :id (:position_id row)
              :name (:position_name row)
              :updated-at (:position_updated_at row)}
   :start (:employment_start row)
   :end (:employment_end row)
   :created-at (:employment_created_at row)
   :updated-at (:employment_updated_at row)})

(defn collect-apprenticeships [apprenticeships row]
  (if-let [apprenticeship (:apprenticeship_id apprenticeships)]
    (let [mentorships (:mentorships apprenticeship)]
      (update-in apprenticeships [:apprenticeship_id :mentorships]
                 (conj (make-mentorship row))))
    (assoc apprenticeships
           (:apprenticeship_id row)
           (make-apprenticeship row))))

(defn rows->apprenticeships [rows]
  (reverse (vals (reduce collect-apprenticeships {} rows))))

(defn- row->engagement [row]
  (-> (data-from-db row engagement-mapping)
      (convert-from-sql-date :start)
      (convert-from-sql-date :end)))

(defn- row->engagement-with-project-and-person [row]
  (let [person (assoc (row->person row)
                      :id (:id_2 row)
                      :created-at (:created_at_2 row)
                      :updated-at (:updated_at_2 row))
        project (assoc (row->project row)
                      :id (:id_3 row)
                      :created-at (:created_at_3 row)
                      :updated-at (:updated_at_3 row))]
    (-> (row->engagement row)
        (assoc :person person)
        (assoc :project project))))

(defn- row->apprenticeship [row]
  (-> (data-from-db row apprenticeship-mapping)
      (convert-from-sql-date :start)
      (convert-from-sql-date :end)))

(defn- row->mentorship [row]
  (-> (data-from-db row mentorship-mapping)
      (convert-from-sql-date :start)
      (convert-from-sql-date :end)))

(def location-membership-mapping
  {:id              :id
   :employment-id   :employment_id
   :location-id     :location_id
   :start           :start
   :created-at      :updated_at
   :updated-at      :updated_at})

(defn row->location-membership [row]
  (-> (data-from-db row location-membership-mapping)
      (convert-from-sql-date :start)
      (convert-from-sql-date :created-at)
      (convert-from-sql-date :updated-at)))

(defn invalid-id? [message fkey-error]
  (or (.contains message fkey-error)
      (invalid-int-error? message)))

(defn duplicate-authentication? [message]
  (.contains message "authentications_unique_uid_provider"))

(defn invalid-user-id? [message]
  (invalid-id? message "authentications_fkey_user_id"))

(defn invalid-group-id? [message]
  (invalid-id? message "group_permissions_fkey_group_id"))

(defn duplicate-group? [message]
  (.contains message "groups_unique_name"))

(defn duplicate-location-name? [message]
  (.contains message "locations_unique_name"))

(defn not-found-location-membership-employment? [message]
  (.contains message "location_memberships_fkey_employment_id"))

(defn not-found-location-membership-location? [message]
  (.contains message "location_memberships_fkey_location_id"))

(defn rescue-employment-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (.contains message "employment_fkey_position_id")
      (failure api [employment-invalid-position-id])
      (.contains message "employment_fkey_person_id")
      (failure api [employment-invalid-person-id])
      (.contains message "location_id")
      (failure api [location-memberships-invalid-location-id])
      (invalid-int-error? message)
      (cond
        (.contains message "person_id")
        (failure api [employment-invalid-person-id])
        (.contains message "position_id")
        (failure api [employment-invalid-position-id])
        :else
        (throw e))
      :else
      (throw e))))

(defn rescue-apprenticeship-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (invalid-id? message "apprenticeship_fkey_person_id")
      (failure api [apprenticeships-invalid-person-id])
      :else (throw e))))

(defn rescue-engagement-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (.contains message "engagements_fkey_employment_id")
      (failure api [engagements-invalid-employment-id])
      (.contains message "engagements_fkey_project_id")
      (failure api [engagements-invalid-project-id])
      (invalid-int-error? message)
      (cond
        (.contains message "employment_id")
        (failure api [engagements-invalid-employment-id])
        (.contains message "project_id")
        (failure api [engagements-invalid-project-id])
        :else
        (throw e))
      :else
      (throw e))))

(defn rescue-director-engagement-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (invalid-id? message "director_engagement_fkey_person_id")
      (failure api [director-engagements-invalid-person-id])
      (invalid-id? message "director_engagement_fkey_project_id")
      (failure api [director-engagements-invalid-project-id])
      (invalid-int-error? message)
      (cond
        (.contains message "person_id")
        (failure api [director-engagements-invalid-person-id])
        (.contains message "project_id")
        (failure api [director-engagements-invalid-project-id])
        :else
        (throw e))
      :else
      (throw e))))

(defn rescue-project-sow-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (.contains message "project_sows_fkey_sow_id")
      (failure api [project-sows-invalid-sow-id])
      (.contains message "project_sows_fkey_project_id")
      (failure api [project-sows-invalid-project-id])
      (invalid-int-error? message)
      (cond
        (.contains message "sow_id")
        (failure api [project-sows-invalid-sow-id])
        (.contains message "project_id")
        (failure api [project-sows-invalid-project-id])
        :else
        (throw e))
      :else
      (throw e))))

(defn rescue-project-skill-save-errors [e api]
  (let [message (.getMessage e)]
    (cond
      (.contains message "project_skills_fkey_skill_id")
      (failure api [project-skills-invalid-skill-id])
      (.contains message "project_skills_fkey_project_id")
      (failure api [project-skills-invalid-project-id])
      (invalid-int-error? message)
      (cond
        (.contains message "skill_id")
        (failure api [project-skills-invalid-skill-id])
        (.contains message "project_id")
        (failure api [project-skills-invalid-project-id])
        :else
        (throw e))
      :else
      (throw e))))

(defn date-range-intersection-filter [start end]
  (if (and start end)
    #(k/where % (or (and (>= :start start)
                         (<= :start end))
                    (and (>= :end start)
                         (<= :end end))
                    (and (<= :start start)
                         (>= :end end))))
    (fn [query] query)))

(defmacro ensure-db [db & body]
  `(if (jdbc/find-connection)
     ~@body
     (kdb/with-db ~db ~@body)))

(defrecord V1MysqlApi [db spec]
  V1Api
  (create-user-with-authentication! [api user-data]
    (try
      (ensure-db db
        (kdb/transaction
          (let [user-id (insert! (users-entity db) user-data user-mapping)
                auth-data (authentication->row (assoc user-data :user-id user-id))
                auth-result (insert! (authentication-entity db) auth-data authentication-mapping)]
            (success api user-id))))
        (catch Exception e
          (if (duplicate-authentication? (.getMessage e))
            (failure api [duplicate-authentication])
            (throw e)))))

  (add-authentication-to-user! [api user-id auth-data]
    (try
      (let [auth-row (authentication->row (assoc auth-data :user-id user-id))
            auth-result (insert! (authentication-entity db) auth-row authentication-mapping)
            result (assoc (select-keys auth-data [:provider :uid]) :user-id user-id)]
        (success api result))
      (catch Exception e
        (let [message (.getMessage e)]
          (cond
            (duplicate-authentication? message)
            (failure api [duplicate-authentication])
            (invalid-user-id? message)
            (not-found api)
            :else
            (throw e))))))

  (find-user-by-provider-and-uid [api provider uid]
    (if-let [user (some-> (k/select (users-entity db)
                                    (k/join :left (authentication-entity db) (= :authentications.user_id :users.id))
                                    (k/where {:authentications.uid [= uid]
                                              :authentications.provider [= (name provider)]})
                                    (k/limit 1))
                          first
                          row->user)]
      (success api user)
      (not-found api)))

  (find-authentications-for-user [api user-id]
    (->> (k/select (authentication-entity db)
                   (k/where {:user_id [= user-id]}))
      (map row->authentication)
      (success api)))

  (find-user-by-id [api user-id]
    (if-let [row (-> (k/select (users-entity db)
                               (k/where {:id [= user-id]})
                               (k/limit 1))
                   first)]
      (success api (row->user row))
      (not-found api)))

  (create-permissions-group! [api group-data]
    (try
      (->> (insert! (group-entity db) group-data group-mapping)
        (success api))
      (catch Exception e
        (if (duplicate-group? (.getMessage e))
          (failure api [duplicate-group-name])
          (throw e)))))

  (find-all-permission-groups [api]
    (->> (k/select (group-entity db))
      (map row->group)
      (success api)))

  (add-permission-to-group! [api options]
    (try
      (do
        (insert! (group-permission-entity db) options group-permission-mapping)
        (success api nil))
      (catch Exception e
        (let [message (.getMessage e)]
          (cond
            (.contains message "Duplicate entry")
            (success api nil)
            (invalid-group-id? message)
            (not-found api)
            :else (throw e))))))

  (find-permissions-for-group [api group-id]
    (->> (k/select (group-permission-entity db)
                   (k/fields :permission)
                   (k/where {:group_id [= group-id]}))
      (map :permission)
      (success api)))

  (find-permission-group-by-id [api group-id]
    (if-let [row (-> (k/select (group-entity db)
                               (k/where {:id [= group-id]})
                               (k/limit 1))
                   first)]
      (success api (row->group row))
      (not-found api)))

  (remove-permission-from-group! [api {:keys [group-id permission]}]
    (do
      (k/delete (group-permission-entity db)
                (k/where {:group_id [= group-id]
                          :permission [= permission]}))
      (success api nil)))

  (add-user-to-group! [api options]
    (try
      (do
        (insert! (group-user-entity db) options group-user-mapping)
        (success api nil))
      (catch Exception e
        (let [message (.getMessage e)]
          (cond
            (.contains message "Duplicate entry")
            (success api nil)
            (or (.contains message "group_users_fkey_group_id")
                (.contains message "group_users_fkey_user_id")
                (invalid-int-error? message))
            (not-found api)
            :else (throw e))))))

  (find-all-users-in-group [api group-id]
    (->> (k/select (users-entity db)
                   (k/join :left (group-user-entity db) (= :group_users.user_id :users.id))
                   (k/where {:group_users.group_id [= group-id]}))
      (map row->user)
      (success api)))

  (find-all-users [api]
    (->> (k/select (users-entity db))
      (map row->user)
      (success api)))

  (remove-user-from-group! [api {:keys [group-id user-id]}]
    (do
      (k/delete (group-user-entity db)
                (k/where {:group_id [= group-id]
                          :user_id [= user-id]}))
      (success api nil)))

  (find-all-groups-for-user [api user-id]
    (->> (k/select (group-entity db)
                   (k/join :left (group-user-entity db) (= :group_users.group_id :groups.id))
                   (k/where {:group_users.user_id [= user-id]}))
      (map row->group)
      (success api)))

  (has-any-permission? [api user-id permissions]
    (->> (k/select (group-user-entity db)
                   (k/fields (k/raw "1"))
                   (k/join :inner (group-permission-entity db) (= :group_users.group_id :group_permissions.group_id))
                   (k/where {:group_users.user_id [= user-id]})
                   (k/where (apply or (for [permission permissions]
                                        {:group_permissions.permission [= permission]})))
                   (k/limit 1))
      seq
      boolean
      (success api)))

  (find-all-permissions-for-user [api user-id]
    (->> (k/select (group-permission-entity db)
                   (k/fields :permission)
                   (k/join :inner (group-user-entity db) (= :group_users.group_id :group_permissions.group_id))
                   (k/where {:group_users.user_id [= user-id]}))
      (map :permission)
      (success api)))

  (create-client! [api client-data]
    (->> (insert! (client-entity db) client-data client-mapping)
      (success api)))

  (update-client! [api client-id client-data]
    (do
      (update! (client-entity db) client-id client-mapping client-data)
      (success api nil)))

  (find-client-by-id [api client-id]
    (if-let [row (-> (k/select (client-entity db)
                               (k/where {:id [= client-id]})
                               (k/limit 1))
                   first)]
      (success api (row->client row))
      (not-found api)))

  (find-all-clients [api]
    (->> (client-entity db)
      k/select
      (map row->client)
      (success api)))

  (delete-client! [api client-id]
    (let [projects (->> (k/select (project-entity db) (k/where {:client_id [= client-id]}))
                        (map row->project))]
      (doseq [project projects]
        (delete-where (project-skill-entity db) :project_id (:id project))
        (delete-where (director-engagement-entity db) :project_id (:id project))))
      (delete-where (project-entity db) :client_id client-id)
      (let [rows-affected (delete-by-id (client-entity db) client-id)]
        (if (not (zero? rows-affected))
          (success api nil)
          (not-found api))))

  (create-skill! [api skill-data]
    (->> (insert! (skill-entity db) skill-data skill-mapping)
      (success api)))

  (update-skill! [api skill-id skill-data]
    (do
      (update! (skill-entity db) skill-id skill-mapping skill-data)
      (success api nil)))

  (find-skill-by-id [api skill-id]
    (if-let [row (-> (k/select (skill-entity db)
                               (k/where {:id [= skill-id]})
                               (k/limit 1))
                   first)]
      (success api (row->skill row))
      (not-found api)))

  (find-all-skills [api]
    (->> (skill-entity db)
      k/select
      (map row->skill)
      (success api)))

  (find-all-skills-for-project [api project-id]
    (success api
      (map row->skill
        (k/select (skill-entity db)
          (k/join :inner :project_skills (= :skills.id :project_skills.skill_id))
          (k/where {:project_skills.project_id [= project-id]})))))

  (create-project! [api project-data]
    (try
      (->> (insert! (project-entity db) project-data project-mapping)
        (success api))
      (catch Exception e
        (let [message (.getMessage e)]
          (if (or (.contains message "projects_fkey_client_id")
                  (invalid-int-error? message))
            (not-found api)
            (throw e))))))

  (find-project-by-id [api project-id]
    (if-let [row (-> (k/select (project-entity db)
                               (k/where {:id [= project-id]})
                               (k/limit 1))
                   first)]
      (success api (row->project row))
      (not-found api)))

  (update-project! [api project-id project-data]
    (let [row (-> project-data (dissoc :client-id))]
      (update! (project-entity db) project-id project-mapping row)
      (success api nil)))

  (find-all-projects [api options]
    (let [sort-field (get {:created-at :created_at
                           :updated-at :updated_at
                           :name       :name} (:sort options))
          sort-direction (get {:asc :ASC :desc :DESC}
                              (:direction options))
          select (k/select* (project-entity db))
          select (if (and sort-field sort-direction)
                   (k/order select sort-field sort-direction)
                   select)]
      (success api
        (if-let [sow-id (:sow-id options)]
          (map row->project
            (k/select (project-entity db)
              (k/join :inner :project_sows (= :projects.id :project_sows.project_id))
              (k/where {:project_sows.sow_id [= sow-id]})))
          (map row->project
            (k/select select))))))

  (find-all-projects-for-client [api client-id]
    (->> (k/select (project-entity db) (k/where {:client_id [= client-id]}))
      (map row->project)
      (success api)))

  (delete-project! [api project-id]
    (delete-where (project-skill-entity db) :project_id project-id)
    (delete-where (director-engagement-entity db) :project_id project-id)
    (let [rows-affected (delete-by-id (project-entity db) project-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (create-sow! [api sow-data]
    (->> (insert! (sow-entity db) sow-data sow-mapping)
      (success api)))

  (update-sow! [api sow-id sow-data]
    (let [rows-updated (update! (sow-entity db) sow-id sow-mapping sow-data)]
      (if (not (zero? rows-updated))
        (success api nil)
        (not-found api))))

  (find-sow-by-id [api sow-id]
    (if-let [row (-> (k/select (sow-entity db)
                               (k/where {:id [= sow-id]})
                               (k/limit 1))
                   first)]
      (success api (row->sow row))
      (not-found api)))

  (find-all-sows [api options]
    (success api
      (if-let [project-id (:project-id options)]
        (map row->sow
            (k/select (sow-entity db)
              (k/join :inner :project_sows (= :sows.id :project_sows.sow_id))
              (k/where {:project_sows.project_id [= project-id]})))
        (map row->sow
          (k/select (sow-entity db))))))

  (delete-sow! [api sow-id]
    (delete-where (project-sow-entity db) :sow_id sow-id)
    (let [rows-affected (delete-by-id (sow-entity db) sow-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (create-project-sow! [api project-sow-data]
    (let [errors (validate-project-sow project-sow-data present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (->> (insert! (project-sow-entity db) project-sow-data project-sow-mapping)
            (success api))
          (catch Exception e
            (rescue-project-sow-save-errors e api))))))

  (update-project-sow! [api project-sow-id project-sow-data]
    (let [ref-present? (fn [data key] (if (contains? data key) (key data) true))
          errors (validate-project-sow project-sow-data ref-present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (let [rows-updated (update! (project-sow-entity db) project-sow-id project-sow-mapping project-sow-data)]
            (if (not (zero? rows-updated))
              (success api nil)
              (not-found api)))
          (catch Exception e
            (rescue-project-sow-save-errors e api))))))

  (find-project-sow-by-id [api project-sow-id]
    (if-let [row (-> (k/select (project-sow-entity db)
                               (k/where {:id [= project-sow-id]})
                               (k/limit 1))
                   first)]
      (success api (row->project-sow row))
      (not-found api)))

  (find-all-project-sows [api]
    (->> (project-sow-entity db)
      k/select
      (map row->project-sow)
      (success api)))

  (delete-project-sow! [api project-sow-id]
    (let [rows-affected (delete-by-id (project-sow-entity db) project-sow-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (delete-project-sows-for-sow! [api sow-id]
    (let [rows-affected (delete-where (project-sow-entity db) :sow_id sow-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (create-project-skill! [api project-skill-data]
    (let [errors (validate-project-skill project-skill-data present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (->> (insert! (project-skill-entity db) project-skill-data project-skill-mapping)
            (success api))
          (catch Exception e
            (rescue-project-skill-save-errors e api))))))

  (find-project-skill-by-id [api project-skill-id]
    (if-let [row (-> (k/select (project-skill-entity db)
                               (k/where {:id [= project-skill-id]})
                               (k/limit 1))
                   first)]
      (success api (row->project-skill row))
      (not-found api)))

  (delete-project-skills-for-project! [api project-id]
    (let [rows-affected (delete-where (project-skill-entity db) :project_id project-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (create-person! [api person-data]
    (->> (insert! (person-entity db) person-data person-mapping)
      (success api)))

  (update-person! [api person-id person-data]
    (let [rows-updated (update! (person-entity db) person-id person-mapping person-data)]
      (if (not (zero? rows-updated))
        (success api nil)
        (not-found api))))

  (find-person-by-id [api person-id]
    (if-let [row (-> (k/select (person-entity db)
                               (k/where {:id [= person-id]})
                               (k/limit 1))
                   first)]
      (success api (row->person row))
      (not-found api)))

  (search-people [api criteria]
    (let [search-attributes (into {} (filter (fn [[k, v]] (and (some #(= k %) (keys person-mapping)) (not (nil? v)))) criteria))
          non-nil-search-attributes (into {} (map (fn [[k, v]] (vector (k person-mapping) ['= v])) search-attributes))
          rows (-> (k/select (person-entity db)
                             (k/where non-nil-search-attributes)))]
        (success api (map #(row->person %) rows))))

  (find-all-people [api]
    (->> (k/select (person-entity db))
      (map row->person)
      (success api)))

  (find-current-people-by-position [api position-name]
    (->> (k/select (person-entity db)
         (k/modifier "DISTINCT")
         (k/join :left (employment-entity db) (= :people.id :employment.person_id))
         (k/join :left (position-entity db) (= :employment.position_id :positions.id))
         (k/where {:positions.name [= position-name]})
         (k/where (or {:employment.end [= nil]}
                      {:employment.end [> (k/sqlfn now)]})))
         (map row->person)
         (success api)))

  (create-employment-position! [api position-data]
    (->> (insert! (position-entity db) position-data position-mapping)
      (success api)))

  (find-all-employment-positions [api]
    (->> (k/select (position-entity db))
      (map row->position)
      (success api)))

  (find-employment-position-by-id [api position-id]
    (if-let [row (-> (k/select (position-entity db)
                               (k/where {:id [= position-id]})
                               (k/limit 1))
                   first)]
      (success api (row->position row))
      (not-found api)))

  (find-employment-position-by-name [api position-name]
    (if-let [row (-> (k/select (position-entity db)
                               (k/where {:name [= position-name]})
                               (k/limit 1))
                   first)]
      (success api (row->position row))
      (not-found api)))

  (create-employment! [api employment-data]
    (let [[employment-data errors] (validate-employment-with-location employment-data present? api)]
       (if (seq errors)
        (failure api errors)
        (try
          (ensure-db db
          (kdb/transaction
            (let [employment (insert! (employment-entity db) employment-data employment-mapping)
                  location-membership-data (make-location-membership-data-from-employment-data employment-data employment)
                  location-membership (insert! (location-membership-entity db) location-membership-data location-membership-mapping)]
              (success api employment))))
        (catch Exception e
          (rescue-employment-save-errors e api))))))

  (update-employment! [api employment-id employment-data]
    (let [ref-present? (fn [data key] (if (contains? data key)
                                        (key data)
                                        true))
          [employment-data errors] (validate-employment employment-data ref-present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (let [rows-updated (update! (employment-entity db) employment-id employment-mapping employment-data)]
            (if (not (zero? rows-updated))
              (success api nil)
              (not-found api)))
          (catch Exception e
            (rescue-employment-save-errors e api))))))

  (find-employment-by-id [api employment-id]
    (if-let [rows (-> (k/exec-raw (kdb/get-connection db)
                                  [(find-employment-sql) [employment-id]] :results)
                    first)]
      (success api (make-employment rows))
      (not-found api)))

  (find-all-employments [api options]
    (let [direction (:direction options)
          location-id (:location-id options)
          location-id (when location-id (str location-id))
          end-date (:end-date options)
          start-date (:start-date options)
          query (-> (k/select* (employment-entity db))
                  (k/fields (field-identifier :employment :*)
                            (field-identifier :people :*)
                            (field-identifier :positions :*))
                  (k/modifier "DISTINCT")
                  (k/join :inner :people (= :employment.person_id :people.id))
                  (k/join :inner :positions (= :employment.position_id :positions.id)))
          query (if start-date
                  (k/where query (or {:employment.end [> start-date]}
                                     {:employment.end [= nil]}))
                  query)
          query (if end-date
                  (k/where query {:employment.start [< end-date]})
                  query)
          query (if location-id
                  (-> query
                    (k/join :inner :location_memberships (= :employment.id :location_memberships.employment_id))
                    (k/where {:location_memberships.location_id [= location-id]}))
                  query)
          query (case (:sort options)
                  :full-name (-> query
                               (k/order :people.first_name direction)
                               (k/order :people.last_name direction))
                  :position (k/order query :positions.name direction)
                  :start (k/order query :employment.start direction)
                  :end (k/order query :employment.end direction)
                  query)
          rows (k/exec query)
          employments
          (map
            (fn [row]
              (let [employment (row->employment row)
                    position (row->position row)
                    person (row->person row)]
                (assoc employment
                       :person (assoc person
                                      :created-at (:created_at_2 row)
                                      :updated-at (:updated_at_2 row)
                                      :id (:id_2 row))
                       :position (assoc position
                                        :created-at (:created_at_3 row)
                                        :updated-at (:updated_at_3 row)
                                        :id (:id_3 row)))))
            rows)

          employments ; location filtered employments
          (if (and location-id (or start-date end-date))
            (filter
              (fn [employment]
                (let [query (-> (k/select* (location-membership-entity db))
                                (k/where {:employment_id [= (:id employment)]})
                                (k/order :start :asc))
                      location-memberships (map row->location-membership (k/exec query))
                      location-memberships (loop [new-location-memberships []
                                                  i 2
                                                  current (first location-memberships)
                                                  next (nth location-memberships 1 nil)]
                                             (if next
                                               (recur
                                                 (conj new-location-memberships (assoc current :end (before (:start next) (days 1))))
                                                 (inc i)
                                                 next
                                                 (nth location-memberships i nil))
                                               (conj new-location-memberships current)))]
                  (some
                    (fn [location-membership]
                      (and
                        (= (str (:location-id location-membership)) location-id)
                        (if end-date
                          (before? (:start location-membership) end-date)
                          true ; start is infinity, which is before the end date
                          )
                        (if (and (:end location-membership) start-date)
                          (after? (:end location-membership) start-date)
                          true ; end is infinity, which is after start date
                          )))
                    location-memberships)))
              employments)
            employments)
          ]
      (success api employments)))

  (find-all-location-memberships-for-employment [api employment-id]
    (->> (k/select (location-membership-entity db) (k/where {:employment_id [= employment-id]}))
      (map row->location-membership)
      (success api)))

  (create-engagement! [api engagement-data]
    (let [engagement-data (assign-default-confidence-pct-if-missing engagement-data)
          errors (validate-engagement engagement-data present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (->> (insert! (engagement-entity db) engagement-data engagement-mapping)
            (success api))
          (catch Exception e
            (rescue-engagement-save-errors e api))))))

  (update-engagement! [api engagement-id engagement-data]
    (let [ref-present? (fn [data key] (if (contains? data key) (key data) true))
          errors (validate-engagement engagement-data ref-present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (let [rows-updated (update! (engagement-entity db) engagement-id engagement-mapping engagement-data)]
            (if (not (zero? rows-updated))
              (success api nil)
              (not-found api)))
          (catch Exception e
            (rescue-engagement-save-errors e api))))))

  (find-engagement-by-id [api engagement-id]
    (if-let [row (-> (k/select (engagement-entity db)
                               (k/where {:id [= engagement-id]})
                               (k/limit 1))
                   first)]
      (success api (row->engagement row))
      (not-found api)))

  (delete-engagement! [api engagement-id]
    (let [rows-affected (delete-by-id (engagement-entity db) engagement-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api))))

  (find-all-engagements [api {:keys [start end project-id]}]
    (let [the-filter (date-range-intersection-filter start end)
          add-constraints (fn [query]
                            (if (nil? project-id)
                              query
                              (k/where query {:project_id [= project-id]})))]
      (->> (k/select (engagement-entity db)
                     (k/fields (field-identifier :engagements :*)
                               (field-identifier :people :*)
                               (field-identifier :projects :*))
                     (k/join :inner (employment-entity db) (= :employment.id :engagements.employment_id))
                     (k/join :inner (person-entity db) (= :people.id :employment.person_id))
                     (k/join :inner (project-entity db) (= :projects.id :engagements.project_id))
                     (add-constraints)
                     the-filter)
           (map row->engagement-with-project-and-person)
           (success api))))

  (create-apprenticeship! [api apprenticeship-data]
    (let [errors (validate-apprenticeship apprenticeship-data present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (let [generated-id (insert! (apprenticeship-entity db) apprenticeship-data apprenticeship-mapping)]
            (doseq [mentorship-data (:mentorships apprenticeship-data)]
              (insert! (mentorship-entity db)
                       (associate-mentorship-to-apprenticeship mentorship-data generated-id apprenticeship-data)
                       mentorship-mapping))
            (success api generated-id))
          (catch Exception e
            (rescue-apprenticeship-save-errors e api))))))

  (find-apprenticeship-by-id [api apprenticeship-id]
      (if-let [rows (seq (k/exec-raw (kdb/get-connection db)
                                     [(find-apprenticeships-sql :where) [apprenticeship-id]] :results))]
        (success api (first (rows->apprenticeships rows)))
        (not-found api)))

  (find-all-apprenticeships [api]
    (let [rows (k/exec-raw (kdb/get-connection db)
                           (find-apprenticeships-sql) :results)]
      (success api (rows->apprenticeships rows))))

  (upcoming-apprentice-graduations-by-location [api]
    (let [all-locations (->> (k/select (location-entity db) (k/fields :name))
                             (map (juxt :name (constantly #{})))
                             (into {}))]
      (as-> upcoming-apprentice-graduations-by-location-sql __
        (k/exec-raw (kdb/get-connection db) __ :results)
        (group-by :location-name __)
        (merge all-locations __)

        (map (fn [[location-name people]]
               {:location-name location-name
                :current-apprentices (set (map (fn [person]
                                                 (-> person
                                                     (dissoc :location-name)
                                                     (convert-from-sql-date :graduates-at)))
                                                people))})
             __)
        (set __)
        (success api __))))

  (create-director-engagement! [api director-engagement-data]
    (let [errors (validate-director-engagement director-engagement-data present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (->> (insert! (director-engagement-entity db) director-engagement-data director-engagement-mapping)
               (success api))
          (catch Exception e
            (rescue-director-engagement-save-errors e api))))))

  (update-director-engagement! [api director-engagement-id director-engagement-data]
    (let [ref-present? (fn [data key] (if (contains? data key) (key data) true))
          errors (validate-director-engagement director-engagement-data ref-present? api)]
      (if (seq errors)
        (failure api errors)
        (try
          (let [rows-updated (update! (director-engagement-entity db) director-engagement-id director-engagement-mapping director-engagement-data)]
            (if (not (zero? rows-updated))
              (success api nil)
              (not-found api)))
          (catch Exception e
            (rescue-director-engagement-save-errors e api))))))

  (find-director-engagement-by-id [api director-engagement-id]
    (if-let [row (-> (k/select (director-engagement-entity db)
                               (k/where {:id [= director-engagement-id]})
                               (k/limit 1))
                   first)]
      (success api (row->director-engagement row))
      (not-found api)))

  (find-all-director-engagements-by-person-id [api director-id]
    (let [result (-> (k/select (director-engagement-entity db)
                               (k/fields (field-identifier :director_engagements :*)
                                         (field-identifier :projects :*))
                               (k/join :inner (project-entity db) (= :projects.id :director_engagements.project_id))
                               (k/where {:director_engagements.person_id [= director-id]})
                               (k/order :director_engagements.start :ASC)))
          mapping-fn #(assoc
                        (row->director-engagement %)
                        :project
                        (assoc
                          (row->project %)
                          :id (:id_2 %)
                          :created-at (:created_at_2 %)
                          :updated-at (:updated_at_2 %)))
          director-engagements (map mapping-fn result)]
      (success api director-engagements)))

  (find-current-directors [api]
    (->> (k/select (person-entity db)
                   (k/fields (field-identifier :people :*))
                   (k/modifier "DISTINCT")
                   (k/join :inner (director-engagement-entity db) (= :director_engagements.person_id :people.id))
                   (k/where {:director_engagements.start [<= (now)]})
                   (k/where (or {:director_engagements.end [> (now)]}
                                {:director_engagements.end nil})))
         (map row->person)
         (success api)))

  (create-location! [api location-data]
    (try (if-let [errors (seq (validate-location location-data present? api))]
           (failure api errors)
           (->> (insert! (location-entity db) location-data location-mapping)
                (success api)))
         (catch Exception e
           (if (duplicate-location-name? (.getMessage e))
             (failure api [locations-duplicate-name])
             (throw e)))))

  (find-all-locations [api]
    (->> (location-entity db)
      k/select
      (map row->location)
      (success api)))

  (find-location-by-id [api location-id]
    (if-let [row (-> (k/select (location-entity db)
                               (k/where {:id [= location-id]})
                               (k/limit 1))
                     (first))]
      (success api (row->location row))
      (not-found api)))

  (create-location-membership! [api employment-id location-id location-membership-data]
    (let [location-membership-data (assoc location-membership-data
                                    :employment-id employment-id
                                    :location-id location-id)]
      (try (if-let [errors (seq (validate-location-membership location-membership-data present? api))]
             (failure api errors)
             (->> (insert! (location-membership-entity db) location-membership-data location-membership-mapping)
                           (success api)))
           (catch Exception e
             [e]
             (cond
               (not-found-location-membership-employment? (.getMessage e)) (failure api [location-memberships-employment-not-found])
               (not-found-location-membership-location? (.getMessage e) (failure api [location-memberships-location-not-found]))
               (throw e))))))

  (find-location-membership-by-id [api location-membership-id]
    (if-let [row (-> (k/select (location-membership-entity db)
                               (k/where {:id [= location-membership-id]})
                               (k/limit 1))
                     (first))]
      (success api (row->location-membership row))
      (not-found api)))

  (find-current-location-membership-for-people [api person-ids]
    (->> (k/select (person-entity db)
         (k/fields [:people.id :person_id]
                   [:locations.name :location_name]
                   [:locations.id :location_id]
                   [:location_memberships.start :location_membership_start])
         (k/join :right (employment-entity db) (= :people.id :employment.person_id))
         (k/join :right (location-membership-entity db) (= :employment.id :location_memberships.employment_id))
         (k/join :inner (location-entity db) (= :location_memberships.location_id :locations.id))
         (k/where {:people.id [in person-ids]}))
         (group-by :person_id)
         (reduce (fn [acc person-with-locations]
           (let [person-id (nth person-with-locations 0)
                 location-memberships (nth person-with-locations 1)
                 current-location-membership (->> location-memberships
                                                  (sort-by :location_membership_start)
                                                  last)]
           (assoc acc person-id {:id (:location_id current-location-membership)
                                 :name (:location_name current-location-membership)})))
           {})
         (success api)))

  (delete-location-membership! [api location-membership-id]
    (let [rows-affected (delete-by-id (location-membership-entity db) location-membership-id)]
      (if (not (zero? rows-affected))
        (success api nil)
        (not-found api)))))

(in-ns 'korma.db)

(import ['com.mchange.v2.c3p0.DataSources]
        ['com.mchange.v2.c3p0.WrapperConnectionPoolDataSource]
        ['com.mchange.v2.sql.SqlUtils])

(defn connection-pool
  "Korma's default way of creating a connection pool does not transfer any \"extra\" properties to the underlying driver. This does the job."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size
           test-connection-query
           idle-connection-test-period
           test-connection-on-checkin
           test-connection-on-checkout]
    :or {excess-timeout (* 30 60)
         idle-timeout (* 3 60 60)
         minimum-pool-size 3
         maximum-pool-size 15
         test-connection-query nil
         idle-connection-test-period 0
         test-connection-on-checkin false
         test-connection-on-checkout false}
    :as spec}]
  (let [url (str "jdbc:" subprotocol ":" subname)
        etc (dissoc spec :subprotocol :subname :classname :user :password
                    :excess-timeout :idle-timeout :minimum-pool-size :maximum-pool-size
                    :test-connection-query
                    :idle-connection-test-period
                    :test-connection-on-checkin
                    :test-connection-on-checkout)
        driver-params (merge etc {com.mchange.v2.sql.SqlUtils/DRIVER_MANAGER_USER_PROPERTY user
                                  com.mchange.v2.sql.SqlUtils/DRIVER_MANAGER_PASSWORD_PROPERTY password})
        unpooled-ds (com.mchange.v2.c3p0.DataSources/unpooledDataSource url (#'jdbc/as-properties driver-params))
        connection-pool-ds (doto (com.mchange.v2.c3p0.WrapperConnectionPoolDataSource.)
                             (.setNestedDataSource unpooled-ds))]
    {:datasource (doto (ComboPooledDataSource.)
                   (.setDriverClass classname)
                   (.setConnectionPoolDataSource connection-pool-ds)
                   (.setMaxIdleTimeExcessConnections excess-timeout)
                   (.setMaxIdleTime idle-timeout)
                   (.setMinPoolSize minimum-pool-size)
                   (.setMaxPoolSize maximum-pool-size)
                   (.setIdleConnectionTestPeriod idle-connection-test-period)
                   (.setTestConnectionOnCheckin test-connection-on-checkin)
                   (.setTestConnectionOnCheckout test-connection-on-checkout)
                   (.setPreferredTestQuery test-connection-query))}))

(in-ns 'stockroom.v1.mysql-api)

(defmacro with-db [api & body]
  `(kdb/with-db (:db ~api) ~@body))

(defn mysql-api [db-spec]
  (let [db-spec (kdb/mysql db-spec)]
    (V1MysqlApi. (kdb/create-db db-spec) db-spec)))
