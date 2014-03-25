; NOTE: in order to use the most up-to-date data for the memory-api, you must always pass the api var along.
; good ex: (let [{api :api person-id-1 :result} (create-person! (api-fn) {:first-name "hello"})
;                {api :api person-id-2 :result} (create-person! api {:first-name "hello"})]
;            (find-person-by-id api person-id-2) ;=> returns person 2
; bad ex: (let [{api :api person-id-1 :result} (create-person! (api-fn) {:first-name "hello"})
;               {person-id-2 :result} (create-person! api {:first-name "hello"})]
;           (find-person-by-id api person-id-2) ;=> returns nil, because the updated api, which is retruned when creating person 2,
;                                               ;   is not used in the finder

(ns stockroom.v1.api-spec
  (:require [speclj.core :as user :refer :all]
            [stockroom.spec-helper :refer [do-at should-implement]]
            [stockroom.util.time :refer [days-ago-at-midnight
                                         days-from-now-at-midnight]]
            [stockroom.v1.api :refer :all]
            [stockroom.v1.response :as response]))

(def twelve-days-ago (days-ago-at-midnight 12))
(def ten-days-ago (days-ago-at-midnight 10))
(def six-days-ago (days-ago-at-midnight 6))
(def five-days-ago (days-ago-at-midnight 5))
(def four-days-ago (days-ago-at-midnight 4))
(def three-days-ago (days-ago-at-midnight 3))
(def two-days-ago (days-ago-at-midnight 2))
(def one-day-ago (days-ago-at-midnight 1))

(def one-day-from-now (days-from-now-at-midnight 1))
(def two-days-from-now (days-from-now-at-midnight 2))
(def three-days-from-now (days-from-now-at-midnight 3))
(def five-days-from-now (days-from-now-at-midnight 5))
(def ten-days-from-now (days-from-now-at-midnight 10))

(defn test-employment
  ([api {:keys [person-id position-id location-id]}]
   (let [{api :api person-id :result} (if (nil? person-id) (create-person! api {:first-name "a"}) {:api api :result person-id})
         {api :api position-id :result} (if (nil? position-id) (create-employment-position! api {:name "test1"}) {:api api :result position-id})
         {api :api location-id :result} (if (nil? location-id) (create-location! api {:name "chicago"}) {:api api :result location-id})
         {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                   :position-id position-id
                                                                   :location-id location-id
                                                                   :start ten-days-ago
                                                                   :end five-days-ago})]
     [api employment-id]))
  ([api]
   (test-employment api {})))

(defn test-engagement [api]
  (let [[api employment-id] (test-employment api)
        {api :api project-id :result} (create-project! api {:name "test"})
        {api :api engagement-id :result} (create-engagement! api {:employment-id employment-id
                                                                  :project-id project-id
                                                                  :start ten-days-ago
                                                                  :end five-days-ago})]
    [api engagement-id]))

(defn test-sow [api]
  (let [{api :api sow-id :result} (create-sow! api {:hourly-rate 100
                                                    :currency-code "USD"
                                                    :start two-days-from-now
                                                    :end three-days-from-now
                                                    :url "www.example.com"
                                                    :signed-date one-day-ago})]
    [api sow-id]))

(defn test-location-membership [api]
  (let [[api employment-id] (test-employment api)
        {api :api location-id :result} (create-location! api {:name "test"})
        {api :api location-membership-id :result} (create-location-membership! api employment-id location-id
                                                                              {:employment-id employment-id
                                                                               :location-id location-id
                                                                               :start ten-days-ago})]
    [api location-membership-id]))

(defmacro defspecs [name args & body]
  `(defn ~name ~args (list ~@body)))

(defmacro should-respond-with-not-found [response api]
  `(do
     (should= :not-found (:status ~response))
     (should-be-nil (:result ~response))
     (should= ~api (:api ~response))))

(defmacro should-fail-with-error [response api error]
  `(do
     (should= :failure (:status ~response))
     (should= ~error (:error ~response))
     (should-be-nil (:result ~response))
     (should= ~api (:api ~response))))

(defmacro should-fail-with-errors [response api errors]
  `(do
     (should= :failure (:status ~response))
     (should= ~errors (:errors ~response))
     (should-be-nil (:result ~response))
     (should= ~api (:api ~response))))

(defmacro should-have-create-timestamps [map]
  `(do
     (should-not-be-nil (:created-at ~map))
     (should= (:created-at ~map)
              (:updated-at ~map))))

(def mentor
  {:first-name "Jane"
   :last-name "Doe"
   :email "john.doe@example.com"})

(def mentee
  {:first-name "John"
   :last-name "Doe"
   :email "jane.doe@example.com"})

(defn create-valid-director-engagement-data [api options]
  (let [project-data (:project options)
        person-data (:person options)
        {api :api person-id :result} (if (or person-data (not (contains? options :person)))
                                       (create-person! api {:first-name "Sandro"
                                                            :last-name "PadinMyStats"})
                                       {:api api :result nil})
        {api :api project-id :result} (if (or project-data (not (contains? options :project)))
                                        (create-project! api (merge {:name "Vision"} project-data))
                                        {:api api :result nil})
        director-engagement-data (merge
                                   {:person-id person-id
                                    :project-id project-id
                                    :start twelve-days-ago
                                    :end one-day-from-now}
                                   options)]
    [api director-engagement-data]))

(defn random-string [prefix]
  #(str prefix "-" (java.util.UUID/randomUUID)))

(defn random-email []
  #(str (java.util.UUID/randomUUID) "@example.com"))

(defmacro defgenerator [generator-name & keys-and-generators]
  `(defn ~generator-name [& kvs#]
     (let [keys-and-generators# (hash-map ~@keys-and-generators)
           overrides# (select-keys (apply hash-map kvs#)
                                   (keys keys-and-generators#))]
       (reduce (fn [acc# [k# gen#]]
                 (assoc acc# k# (k# overrides# (gen#))))
               {}
               keys-and-generators#))))

(defgenerator create-valid-location-data
  :name (random-string "location-name"))

(defgenerator create-valid-location-membership-data
  :start (constantly one-day-ago))

(defgenerator create-valid-person-data
  :first-name (random-string "first-name")
  :last-name (random-string "last-name")
  :email (random-email))

(defspecs api-spec [api-fn]

  (it "implements the whole api"
    (should-implement V1Api (class (api-fn))))

  (it "creates a user with a provider and uid"
    (let [response (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123" :name "test name"})
          {api :api user-id :result} response
          {user :result} (find-user-by-id api user-id)]

      (should= :success (:status response))
      (should= user-id (:id user))
      (should= "test name" (:name user))
      (should-have-create-timestamps user)))

  (it "creates a user ignores the given id field"
    (let [response1 (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123" :id nil})
          {api :api user-id :result status1 :status} response1
          response2 (find-user-by-provider-and-uid api :google "abc123")
          {api :api user :result status2 :status} response2]
      (should= user-id (:id (:result response2)))))

  (it "finds a user by provider and uid"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123" :name "test name"})
          find-response (find-user-by-provider-and-uid api :google "abc123")
          user (:result find-response)]

      (should= :success (:status find-response))
      (should= api (:api find-response))
      (should= user-id (:id user))
      (should= "test name" (:name user))
      (should-have-create-timestamps user)))

  (it "find-user-by-provider-and-uid responds with not found when there is no match provider and uid"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123" :id nil})
          response1 (find-user-by-provider-and-uid api :google "abc124") ; matching provider but not uid
          response2 (find-user-by-provider-and-uid api :twitter "abc123") ; matching uid but not provider
          response3 (find-user-by-provider-and-uid api :gplus "432") ; neither
          ]
      (doseq [response [response1 response2 response3]]
        (should-respond-with-not-found response api))))

  (it "fails to create the user if another user already has the same provider and uid"
    (let [response1 (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          {api :api} response1
          response2 (create-user-with-authentication! api {:provider :google :uid "abc123"})]

      (should= :success (:status response1))
      (should-fail-with-errors response2 api [response/duplicate-authentication])))

  (it "returns all users"
    (let [{api :api user-id1 :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          {api :api user-id2 :result} (create-user-with-authentication! api {:provider :google :uid "abc124"})
          find-response (find-all-users api)]

      (should= :success (:status find-response))
      (should== [user-id1 user-id2] (map :id (:result find-response)))))

  (it "adds an authentication to a user"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          add-response (add-authentication-to-user! api user-id {:provider :twitter :uid "abc123"})
          {api :api} add-response]

      (should= :success (:status add-response))
      (should= {:provider :twitter :uid "abc123" :user-id user-id} (:result add-response))
      (should= api (:api add-response))
      (should== [{:provider :google :uid "abc123" :user-id user-id}
                 {:provider :twitter :uid "abc123" :user-id user-id}]
                (map
                  #(select-keys % [:provider :uid :user-id])
                  (:result (find-authentications-for-user (:api add-response) user-id))))))

  (it "can add two authentications for the same provider"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          add-response (add-authentication-to-user! api user-id {:provider :google :uid "abc124"})]

      (should= :success (:status add-response))
      (should= {:provider :google :uid "abc124" :user-id user-id} (:result add-response))
      (should== [{:provider :google :uid "abc123" :user-id user-id}
                 {:provider :google :uid "abc124" :user-id user-id}]
                (map
                  #(select-keys % [:provider :uid :user-id])
                  (:result (find-authentications-for-user (:api add-response) user-id))))))

  (it "fails to add an authentication to user when it is already taken by that user"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          add-response (add-authentication-to-user! api user-id {:provider :google :uid "abc123"})]
      (should-fail-with-errors add-response api [response/duplicate-authentication])))

  (it "fails to add an authentication to user when it is already taken by another user"
    (let [{api :api user-id1 :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          {api :api user-id2 :result} (create-user-with-authentication! api {:provider :google :uid "abc124"})
          add-response (add-authentication-to-user! api user-id2 {:provider :google :uid "abc123"})]
      (should-fail-with-errors add-response api [response/duplicate-authentication])))

  (it "responds with a not found when the user does not exist"
    (let [add-response1 (add-authentication-to-user! (api-fn) "10" {:provider :google :uid "abc123"})
          add-response2 (add-authentication-to-user! (api-fn) "bad-id" {:provider :google :uid "abc123"})]
      (should-respond-with-not-found add-response1 (api-fn))
      (should-respond-with-not-found add-response2 (api-fn))))

  (it "loads the authentications for a user"
    (let [response1 (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          {api :api user-id :result} response1
          response2 (add-authentication-to-user! api user-id {:provider :twitter :uid "abc123"})
          response3 (find-authentications-for-user (:api response2) user-id)]

      (should= :success (:status response3))
      (should== [{:provider :google :uid "abc123" :user-id user-id}
                 {:provider :twitter :uid "abc123" :user-id user-id}]
                (map
                  #(select-keys % [:provider :uid :user-id])
                  (:result response3)))))

  (it "loads a user by id"
    (let [response1 (create-user-with-authentication! (api-fn) {:provider :google :uid "abc123"})
          {api :api user-id :result} response1
          response2 (find-user-by-id api user-id)]
      (should= :success (:status response2))
      (should= user-id (:id (:result response2)))))

  (it "responds with not found when the user does not exist"
    (let [response1 (find-user-by-id (api-fn) "10")
          response2 (find-user-by-id (api-fn) "bad-id")]
      (should-respond-with-not-found response1 (api-fn))
      (should-respond-with-not-found response2 (api-fn))))

  (it "creates a group"
    (let [create-response (create-permissions-group! (api-fn) {:name "Admins"})
          {api-after-create :api group-id :result create-status :status} create-response
          find-response (find-all-permission-groups api-after-create)
          {api-after-find :api groups :result find-status :status} find-response
          found-group (first (filter #(= (:id %) group-id) groups))]

      (should= :success create-status)
      (should= :success find-status)
      (should= api-after-create api-after-find)
      (should= 1 (count groups))
      (should= "Admins" (:name found-group))
      (should= group-id (:id found-group))))

  (it "does not create a group when the name is duplicated"
    (let [first-create-response (create-permissions-group! (api-fn) {:name "Admins"})
          {api :api} first-create-response
          second-create-response (create-permissions-group! api {:name "Admins"})]

      (should= :success (:status first-create-response))
      (should-fail-with-errors second-create-response api [response/duplicate-group-name])
      (should= 1 (count (:result (find-all-permission-groups (:api second-create-response)))))))

  (it "adds a permission to a group"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          create-response (add-permission-to-group! api {:group-id group-id
                                                         :permission "users/edit"})
          {api :api} create-response
          find-response (find-permissions-for-group api group-id)]

      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should-be-nil (:result create-response))
      (should= ["users/edit"] (:result find-response))
      (should= (:api create-response)
               (:api find-response))))

  (it "does nothing when the permission already exists in the group"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          {api :api} (add-permission-to-group! api {:group-id group-id
                                                    :permission "users/edit"})
          second-create-response (add-permission-to-group! api {:group-id group-id
                                                                :permission "users/edit"})
          {api :api} second-create-response
          {permissions :result} (find-permissions-for-group api group-id)]

      (should= :success (:status second-create-response))
      (should-be-nil (:result second-create-response))
      (should= ["users/edit"] permissions)))

  (it "add-permission-to-group! responds with not found when group does not exist or is invalid"
    (let [response1 (add-permission-to-group! (api-fn) {:group-id "bad-id"  :permission "users/edit"})
          response2 (add-permission-to-group! (api-fn) {:group-id "10" :permission "users/edit"})]
      (should-respond-with-not-found response1 (api-fn))
      (should-respond-with-not-found response2 (api-fn))))

  (it "finds a group by id"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          find-response (find-permission-group-by-id api group-id)
          group (:result find-response)]

      (should= :success (:status find-response))
      (should= group-id (:id group))
      (should= "test" (:name group))
      (should-have-create-timestamps group)
      (should= api (:api find-response))))

  (it "returns a not found when group does not exist"
    (let [find-response1 (find-permission-group-by-id (api-fn) "bad-id")
          find-response2 (find-permission-group-by-id (api-fn) "10")]
      (should-respond-with-not-found find-response1 (api-fn))
      (should-respond-with-not-found find-response2 (api-fn))))

  (it "removes a permission from a group"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          {api :api} (add-permission-to-group! api {:group-id group-id :permission "users/edit"})
          remove-response (remove-permission-from-group! api {:group-id group-id :permission "users/edit"})
          {api :api} remove-response]

      (should= :success (:status remove-response))
      (should-be-nil (:result remove-response))
      (should= [] (:result (find-permissions-for-group api group-id)))))

  (it "does nothing when there is no matching group and permission"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          remove-response (remove-permission-from-group! api {:group-id group-id :permission "users/edit"})]
      (should= :success (:status remove-response))
      (should= api (:api remove-response))))

  (it "adds a user to a group"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "123"})
          {api :api group-id :result} (create-permissions-group! api {:name "test"})
          add-response (add-user-to-group! api {:group-id group-id :user-id user-id})]

      (should= :success (:status add-response))
      (should-be-nil (:result add-response))
      (should= [user-id]
               (->> (find-all-users-in-group (:api add-response) group-id)
                 :result
                 (map :id)))))

  (it "does nothing when the user is already in the group"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "123"})
          {api :api group-id :result} (create-permissions-group! api {:name "test"})
          add-response1 (add-user-to-group! api {:group-id group-id :user-id user-id})
          {api :api} add-response1
          add-response2 (add-user-to-group! api {:group-id group-id :user-id user-id})]

      (should= :success (:status add-response2))
      (should-be-nil (:result add-response2))
      (should= [user-id]
               (->> (find-all-users-in-group (:api add-response2) group-id)
                 :result
                 (map :id)))))

  (it "responds with not found when the group does not exist"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "123"})
          add-response1 (add-user-to-group! api {:group-id "10" :user-id user-id})
          add-response2 (add-user-to-group! api {:group-id "bad-id" :user-id user-id})]
      (should-respond-with-not-found add-response1 api)
      (should-respond-with-not-found add-response2 api)))

  (it "responds with not found when the user does not exist"
    (let [{api :api group-id :result} (create-permissions-group! (api-fn) {:name "test"})
          add-response1 (add-user-to-group! api {:group-id group-id :user-id "10"})
          add-response2 (add-user-to-group! api {:group-id group-id :user-id "bad-id"})]
      (should-respond-with-not-found add-response1 api)
      (should-respond-with-not-found add-response2 api)))

  (it "responds with not found when neither the user or group exists"
    (let [add-response1 (add-user-to-group! (api-fn) {:group-id "10" :user-id "10"})
          add-response2 (add-user-to-group! (api-fn) {:group-id "bad-id" :user-id "bad-id"})]
      (should-respond-with-not-found add-response1 (api-fn))
      (should-respond-with-not-found add-response2 (api-fn))))

  (it "removes a user from a group"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc"})
          {api :api group-id :result} (create-permissions-group! api {:name "test"})
          {api :api} (add-user-to-group! api {:group-id group-id :user-id user-id})
          remove-response (remove-user-from-group! api {:group-id group-id :user-id user-id})
          {api :api} remove-response]

      (should= :success (:status remove-response))
      (should-be-nil (:result remove-response))
      (should= [] (:result (find-all-users-in-group api group-id)))))

  (it "does nothing when there is no matching user and group"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc"})
          {api :api group-id :result} (create-permissions-group! api {:name "test"})
          remove-response (remove-user-from-group! api {:group-id group-id :user-id user-id})
          {api :api} remove-response]

      (should= :success (:status remove-response))
      (should-be-nil (:result remove-response))))

  (it "returns all groups for a user"
    (let [{api :api user-id :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc"})
          {api :api group-id1 :result} (create-permissions-group! api {:name "test1"})
          {api :api group-id2 :result} (create-permissions-group! api {:name "test2"})
          {api :api} (add-user-to-group! api {:group-id group-id1 :user-id user-id})
          {api :api} (add-user-to-group! api {:group-id group-id2 :user-id user-id})]

      (should== [group-id1 group-id2]
                (map :id (:result (find-all-groups-for-user api user-id))))))

  (it "has-any-permission? returns true if the has one of the permissions given"
    (let [{api :api user-id1 :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc"})
          {api :api user-id2 :result} (create-user-with-authentication! api {:provider :google :uid "abc1"})
          {api :api group-id1 :result} (create-permissions-group! api {:name "test1"})
          {api :api} (add-permission-to-group! api {:permission "perm1" :group-id group-id1})
          {api :api group-id2 :result} (create-permissions-group! api {:name "test2"})
          {api :api} (add-permission-to-group! api {:permission "perm2" :group-id group-id2})
          {api :api} (add-user-to-group! api {:group-id group-id1 :user-id user-id1})
          {api :api} (add-user-to-group! api {:group-id group-id2 :user-id user-id2})
          true-response1 (has-any-permission? api user-id1 ["perm1" "perm2"])
          true-response2 (has-any-permission? api user-id2 ["perm2" "perm1"])
          false-response1 (has-any-permission? api user-id1 ["perm2"])
          false-response2 (has-any-permission? api user-id2 ["perm1"])
          false-response3 (has-any-permission? api user-id1 ["unknown-permission"])
          false-response4 (has-any-permission? api "bad-id" ["perm1"])
          false-response5 (has-any-permission? api "10" ["perm1"])]

      (doseq [response [true-response1 true-response2]]
        (should= :success (:status response))
        (should= true (:result response))
        (should= api (:api response)))

      (doseq [response [false-response1 false-response2 false-response3 false-response4 false-response5]]
        (should= :success (:status response))
        (should= false (:result response))
        (should= api (:api response)))))

  (it "returns all permissions for a user"
    (let [{api :api user-id1 :result} (create-user-with-authentication! (api-fn) {:provider :google :uid "abc"})
          {api :api user-id2 :result} (create-user-with-authentication! api {:provider :google :uid "abc1"})
          {api :api group-id1 :result} (create-permissions-group! api {:name "test1"})
          {api :api} (add-permission-to-group! api {:permission "perm1" :group-id group-id1})
          {api :api group-id2 :result} (create-permissions-group! api {:name "test2"})
          {api :api} (add-permission-to-group! api {:permission "perm2" :group-id group-id2})
          {api :api} (add-permission-to-group! api {:permission "perm3" :group-id group-id2})
          {api :api} (add-user-to-group! api {:group-id group-id1 :user-id user-id1})
          {api :api} (add-user-to-group! api {:group-id group-id1 :user-id user-id2})
          {api :api} (add-user-to-group! api {:group-id group-id2 :user-id user-id2})
          {api :api user-1-permissions :result} (find-all-permissions-for-user api user-id1)
          {api :api user-2-permissions :result} (find-all-permissions-for-user api user-id2)]

      (should= ["perm1"] user-1-permissions)
      (should== ["perm1" "perm2" "perm3"] user-2-permissions)))

  (it "creates and finds a client"
    (let [create-response (create-client! (api-fn) {:name "Test Client"})
          {api :api client-id :result} create-response
          find-response (find-client-by-id api client-id)
          created-client (:result find-response)]
      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should= client-id (:id created-client))
      (should= "Test Client" (:name created-client))
      (should-not-be-nil (:created-at created-client))
      (should= (:created-at created-client)
               (:updated-at created-client))))

  (it "updates a client"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {created-client :result} (find-client-by-id api client-id)
          update-response (update-client! api client-id {:name "Updated Name" :id "bad-id"})
          {api :api} update-response
          {updated-client :result} (find-client-by-id api client-id)]
      (should= :success (:status update-response))
      (should-be-nil (:result update-response))
      (should= client-id (:id updated-client))
      (should= "Updated Name" (:name updated-client))
      ;(should-not= (:updated-at updated-client)
      ;             (:created-at updated-client))
      (should= (:created-at updated-client)
               (:created-at created-client))))

  (it "find-client-by-id responds with not found when there is no matching client"
    (let [find-response1 (find-client-by-id (api-fn) "bad-id")
          find-response2 (find-client-by-id (api-fn) "10")]
      (should-respond-with-not-found find-response1 (api-fn))
      (should-respond-with-not-found find-response2 (api-fn))))

  (it "lists all clients"
    (let [{api :api client-id1 :result} (create-client! (api-fn) {:name "Test Client1"})
          {client1 :result} (find-client-by-id api client-id1)
          {api :api client-id2 :result} (create-client! api {:name "Test Client2"})
          {client2 :result} (find-client-by-id api client-id2)
          find-all-response (find-all-clients api)]
      (should== [client1 client2]
                (:result find-all-response))
      (should= :success (:status find-all-response))
      (should= api (:api find-all-response))))

  (it "deletes a client"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api :as delete-response} (delete-client! api client-id)
          find-response (find-client-by-id api client-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))

  (it "deletes all related projects when deleting a client"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project1-id :result} (create-project! api {:name "Test Project 1" :client-id client-id})
          {api :api project2-id :result} (create-project! api {:name "Test Project 2" :client-id client-id})
          {api :api :as delete-response} (delete-client! api client-id)
          find-project1-response (find-project-by-id api project1-id)
          find-project2-response (find-project-by-id api project2-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-project1-response api)
      (should-respond-with-not-found find-project2-response api)))

  (it "deletes all related project-skills when deleting a client"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project-id :result} (create-project! api {:name "Test Project 1" :client-id client-id})
          {api :api skill1-id :result} (create-skill! api {:name "Test Skill 1"})
          {api :api skill2-id :result} (create-skill! api {:name "Test Skill 2"})
          {api :api project-skill1-id :result} (create-project-skill! api {:skill-id skill1-id :project-id project-id})
          {api :api project-skill2-id :result} (create-project-skill! api {:skill-id skill2-id :project-id project-id})
          {api :api :as delete-response} (delete-client! api client-id)
          find-client-response (find-client-by-id api client-id)
          find-project-response (find-project-by-id api project-id)
          find-project-skill1-response (find-project-skill-by-id api project-skill1-id)
          find-project-skill2-response (find-project-skill-by-id api project-skill2-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-client-response api)
      (should-respond-with-not-found find-project-response api)
      (should-respond-with-not-found find-project-skill1-response api)
      (should-respond-with-not-found find-project-skill2-response api)))

  (it "deletes all related director engagements when deleting a client"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project-id :result} (create-project! api {:name "Test Project 1" :client-id client-id})
          {api :api person-id :result} (create-person! api {:first-name "Nicole" :last-name "Carp"})
          {api :api director-engagement-id :result} (create-director-engagement! api {:person-id person-id
                                                                                      :project-id project-id
                                                                                      :start five-days-ago})
          {api :api director-engagement :result} (find-director-engagement-by-id api director-engagement-id)
          {api :api :as delete-response} (delete-client! api client-id)
          find-client-response (find-client-by-id api client-id)
          find-project-response (find-project-by-id api project-id)
          find-director-engagement-response (find-director-engagement-by-id api director-engagement-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-client-response api)
      (should-respond-with-not-found find-project-response api)
      (should-respond-with-not-found find-director-engagement-response api)))

  (it "responds with not found when attempting to delete a client that does not exist"
    (let [api (api-fn)
          delete-response (delete-client! api "bad-id")]
      (should-respond-with-not-found delete-response api)))

  (it "creates and finds a skill"
    (let [create-response (create-skill! (api-fn) {:name "Test Skill"})
          {api :api skill-id :result} create-response
          find-response (find-skill-by-id api skill-id)
          created-skill (:result find-response)]
      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should= skill-id (:id created-skill))
      (should= "Test Skill" (:name created-skill))
      (should-not-be-nil (:created-at created-skill))
      (should= (:created-at created-skill)
               (:updated-at created-skill))))

  (it "updates a skill"
    (let [{api :api skill-id :result} (create-skill! (api-fn) {:name "Test Skill"})
          {created-skill :result} (find-skill-by-id api skill-id)
          update-response (update-skill! api skill-id {:name "Updated Name" :id "bad-id"})
          {api :api} update-response
          {updated-skill :result} (find-skill-by-id api skill-id)]
      (should= :success (:status update-response))
      (should-be-nil (:result update-response))
      (should= skill-id (:id updated-skill))
      (should= "Updated Name" (:name updated-skill))
      (should= (:created-at updated-skill)
               (:created-at created-skill))))

  (it "find-skill-by-id responds with not found when there is no matching skill"
    (let [find-response1 (find-skill-by-id (api-fn) "bad-id")
          find-response2 (find-skill-by-id (api-fn) "10")]
      (should-respond-with-not-found find-response1 (api-fn))
      (should-respond-with-not-found find-response2 (api-fn))))

  (it "lists all skills"
    (let [{api :api skill-id1 :result} (create-skill! (api-fn) {:name "Test Skill1"})
          {skill1 :result} (find-skill-by-id api skill-id1)
          {api :api skill-id2 :result} (create-skill! api {:name "Test Skill2"})
          {skill2 :result} (find-skill-by-id api skill-id2)
          find-all-response (find-all-skills api)]
      (should== [skill1 skill2]
                (:result find-all-response))
      (should= :success (:status find-all-response))
      (should= api (:api find-all-response))))

  (it "creates and finds a project"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          create-response (create-project! api {:name "Test Project" :source-url "http://project.com" :client-id client-id})
          {api :api project-id :result} create-response
          find-response (find-project-by-id api project-id)
          created-project (:result find-response)]
      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should= project-id (:id created-project))
      (should= "Test Project" (:name created-project))
      (should= "http://project.com" (:source-url created-project))
      (should= client-id (:client-id created-project))
      (should-not-be-nil (:created-at created-project))
      (should= (:created-at created-project)
               (:updated-at created-project))))

  (it "create-project! creates a project without a client"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "Test Project"})
          {api :api project :result} (find-project-by-id api project-id)]
      (should= project-id (:id project))
      (should= "Test Project" (:name project))
      (should-be-nil (:client-id project))))

  (it "create-project! responds with not found if there is no client"
    (let [create-response1 (create-project! (api-fn) {:name "Test Project" :client-id "bad-id"})
          create-response2 (create-project! (api-fn) {:name "Test Project" :client-id "10"})]
      (should-respond-with-not-found create-response1 (api-fn))
      (should-respond-with-not-found create-response2 (api-fn))))

  (it "find-project-by-id response with not found with the project cannot be found"
    (let [find-response1 (find-project-by-id (api-fn) "10")
          find-response2 (find-project-by-id (api-fn) "bad-id")]
      (should-respond-with-not-found find-response1 (api-fn))
      (should-respond-with-not-found find-response2 (api-fn))))

  (it "updates a project"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "Test Project"})
          {created-project :result} (find-project-by-id api project-id)
          update-response (update-project! api project-id {:name "Updated Name" :id "bad-id"})
          {api :api} update-response
          {updated-project :result} (find-project-by-id api project-id)]
      (should= :success (:status update-response))
      (should-be-nil (:result update-response))
      (should= project-id (:id updated-project))
      (should= "Updated Name" (:name updated-project))
      (should= (:created-at updated-project)
               (:created-at created-project))))

  (it "only updates the name of a project - not client-id"
    (let [{api :api client-id1 :result} (create-client! (api-fn) {:name "Test Client1"})
          {api :api client-id2 :result} (create-client! api {:name "Test Client2"})
          {api :api project-id :result} (create-project! api {:name "Test Project" :client-id client-id1})
          {created-project :result} (find-project-by-id api project-id)
          update-response (update-project! api project-id {:name "Updated Name" :client-id client-id2})
          {api :api} update-response
          {updated-project :result} (find-project-by-id api project-id)]
      (should= :success (:status update-response))
      (should-be-nil (:result update-response))
      (should= project-id (:id updated-project))
      (should= client-id1 (:client-id updated-project))
      (should= "Updated Name" (:name updated-project))
      (should= (:created-at updated-project)
               (:created-at created-project))))

  (it "finds all projects for a client"
    (let [{api :api client-id1 :result} (create-client! (api-fn) {:name "Test Client1"})
          {api :api client-id2 :result} (create-client! api {:name "Test Client1"})
          {api :api project-id1 :result} (create-project! api {:name "Test project1" :client-id client-id1})
          {project1 :result} (find-project-by-id api project-id1)
          {api :api project-id2 :result} (create-project! api {:name "Test project2" :client-id client-id2})
          {project2 :result} (find-project-by-id api project-id2)
          find-all-response (find-all-projects-for-client api client-id1)]
      (should== [project1] (:result find-all-response))
      (should= :success (:status find-all-response))
      (should= api (:api find-all-response))
      (should= [project2] (:result (find-all-projects-for-client api client-id2)))))

  (it "finds all projects"
    (let [{api :api client-id1 :result} (create-client! (api-fn) {:name "Test Client1"})
          {api :api client-id2 :result} (create-client! api {:name "Test Client1"})
          {api :api project-id1 :result} (create-project! api {:name "Test project1" :client-id client-id1})
          {project1 :result} (find-project-by-id api project-id1)
          {api :api project-id2 :result} (create-project! api {:name "Test project2" :client-id client-id2})
          {project2 :result} (find-project-by-id api project-id2)
          find-all-response (find-all-projects api {})]
      (should= :success (:status find-all-response))
      (should== [project1 project2] (:result find-all-response))))

  (it "finds all projects for a sow"
    (let [{api :api sow1-id :result} (create-sow! (api-fn) {:hourly-rate 200
                                                            :currency-code "USD"
                                                            :start one-day-from-now
                                                            :end two-days-from-now
                                                            :url "www.example.com"})
          {api :api sow1 :result} (find-sow-by-id api sow1-id)
          {api :api sow2-id :result} (create-sow! api {:hourly-rate 175
                                                       :currency-code "USD"
                                                       :start two-days-from-now
                                                       :end three-days-from-now
                                                       :url "www.examples.com"
                                                       :signed-date one-day-ago})
          {api :api sow2 :result} (find-sow-by-id api sow2-id)
          {api :api client-id :result} (create-client! api {:name "Test Client1"})
          {api :api project1-id :result} (create-project! api {:name "Test project1" :client-id client-id})
          {project1 :result} (find-project-by-id api project1-id)
          {api :api project2-id :result} (create-project! api {:name "Test project2" :client-id client-id})
          {project2 :result} (find-project-by-id api project2-id)
          {api :api project3-id :result} (create-project! api {:name "Test project3" :client-id client-id})
          {project3 :result} (find-project-by-id api project3-id)
          {api :api project-sow1 :result} (create-project-sow! api {:sow-id sow1-id :project-id project1-id})
          {api :api project-sow2 :result} (create-project-sow! api {:sow-id sow2-id :project-id project2-id})
          {api :api project-sow3 :result} (create-project-sow! api {:sow-id sow1-id :project-id project3-id})
          find-all-response (find-all-projects api {:sow-id sow1-id})]
      (should= :success (:status find-all-response))
      (should== [project1 project3] (:result find-all-response))))

  (it "sorts projects"
    (let [{api :api client-id1 :result} (create-client! (api-fn) {:name "Test Client1"})
          {api :api client-id2 :result} (create-client! api {:name "Test Client1"})
          {api :api project-id1 :result} (do-at four-days-ago (create-project! api {:name "abc" :client-id client-id1}))
          {project1 :result} (find-project-by-id api project-id1)
          {api :api project-id2 :result} (do-at two-days-ago (create-project! api {:name "xyz" :client-id client-id2}))
          {project2 :result} (find-project-by-id api project-id2)]

      (should= [project1 project2] (:result (find-all-projects api {:sort :created-at :direction :asc})))
      (should= [project2 project1] (:result (find-all-projects api {:sort :created-at :direction :desc})))
      (should= [project1 project2] (:result (find-all-projects api {:sort :updated-at :direction :asc})))
      (should= [project2 project1] (:result (find-all-projects api {:sort :updated-at :direction :desc})))
      (should= [project1 project2] (:result (find-all-projects api {:sort :name :direction :asc})))
      (should= [project2 project1] (:result (find-all-projects api {:sort :name :direction :desc})))))

  (it "deletes a project"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project-id :result} (create-project! api {:name "Test Project" :client-id client-id})
          {api :api :as delete-response} (delete-project! api project-id)
          find-response (find-project-by-id api project-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))

  (it "deletes all related project-skills when deleting a project"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project-id :result} (create-project! api {:name "Test Project" :client-id client-id})
          {api :api skill1-id :result} (create-skill! api {:name "Test Skill 1"})
          {api :api skill2-id :result} (create-skill! api {:name "Test Skill 2"})
          {api :api project-skill1-id :result} (create-project-skill! api {:skill-id skill1-id :project-id project-id})
          {api :api project-skill2-id :result} (create-project-skill! api {:skill-id skill2-id :project-id project-id})
          {api :api :as delete-response} (delete-project! api project-id)
          find-project-skill1-response (find-project-skill-by-id api project-skill1-id)
          find-project-skill2-response (find-project-skill-by-id api project-skill2-id)]
      (should-respond-with-not-found find-project-skill1-response api)
      (should-respond-with-not-found find-project-skill2-response api)))

  (it "deletes all related director engagements when deleting a project"
    (let [{api :api client-id :result} (create-client! (api-fn) {:name "Test Client"})
          {api :api project-id :result} (create-project! api {:name "Test Project 1" :client-id client-id})
          {api :api person-id :result} (create-person! api {:first-name "Nicole" :last-name "Carp"})
          {api :api director-engagement-id :result} (create-director-engagement! api {:person-id person-id
                                                                                      :project-id project-id
                                                                                      :start five-days-ago})
          {api :api director-engagement :result} (find-director-engagement-by-id api director-engagement-id)
          {api :api :as delete-response} (delete-project! api project-id)
          find-project-response (find-project-by-id api project-id)
          find-director-engagement-response (find-director-engagement-by-id api director-engagement-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-project-response api)
      (should-respond-with-not-found find-director-engagement-response api)))

  (it "responds with not found when attempting to delete a project that does not exist"
    (let [api (api-fn)
          delete-response (delete-project! api "bad-id")]
      (should-respond-with-not-found delete-response api)))

  (it "creates a SOW"
    (let [start two-days-from-now
          end three-days-from-now
          {api :api sow-id :result status :status} (create-sow! (api-fn) {:hourly-rate 200
                                                                          :currency-code "USD"
                                                                          :start start
                                                                          :end end
                                                                          :url "www.example.com"})
          {api :api sow :result} (find-sow-by-id api sow-id)]
      (should= :success status)
      (should= sow-id (:id sow))
      (should= 200 (:hourly-rate sow))
      (should= "USD" (:currency-code sow))
      (should= start (:start sow))
      (should= end (:end sow))
      (should= "www.example.com" (:url sow))
      (should-have-create-timestamps sow)))

  (it "updates a SOW"
    (let [start two-days-from-now
          end three-days-from-now
          signed-date one-day-ago
          {api :api sow-id :result status :status} (create-sow! (api-fn) {:hourly-rate 200
                                                                          :currency-code "USD"
                                                                          :start start
                                                                          :end end
                                                                          :url "www.example.com"
                                                                          :signed-date signed-date})
          {created-sow :result} (find-sow-by-id api sow-id)
          {api :api :as response} (update-sow! api sow-id {:hourly-rate 180
                                                           :url "www.example2.com"
                                                           :signed-date one-day-ago})
          {updated-sow :result} (find-sow-by-id api sow-id)]
      (should= :success (:status response))
      (should= sow-id (:id updated-sow))
      (should= 180 (:hourly-rate updated-sow))
      (should= "USD" (:currency-code updated-sow))
      (should= "www.example2.com" (:url updated-sow))
      (should= signed-date (:signed-date updated-sow))))

  (it "responds with not found when updating a sow that cannot be found"
    (let [response (update-sow! (api-fn) "bad-id" {:hourly-rate 180
                                                   :currency-code "USD"
                                                   :url "www.example2.com"
                                                   :signed-date one-day-ago})]
      (should-respond-with-not-found response (api-fn))))

  (it "finds a SOW by id"
    (let [start two-days-from-now
          end three-days-from-now
          {api :api sow-id :result} (create-sow! (api-fn) {:hourly-rate 200
                                                           :currency-code "USD"
                                                           :start start
                                                           :end end
                                                           :url "www.example.com"})
          find-response (find-sow-by-id api sow-id)
          sow (:result find-response)]
      (should= :success (:status find-response))
      (should= sow-id (:id sow))
      (should= 200 (:hourly-rate sow))
      (should= "USD" (:currency-code sow))
      (should= start (:start sow))
      (should= end (:end sow))
      (should= "www.example.com" (:url sow))
      (should-have-create-timestamps sow)
      (should= api (:api find-response))))

  (it "responds with not found when the SOW does not exist"
    (let [response1 (find-sow-by-id (api-fn) "bad-id")
          response2 (find-sow-by-id (api-fn) "10")]
      (should-respond-with-not-found response1 (api-fn))
      (should-respond-with-not-found response2 (api-fn))))

  (it "deletes a SOW"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api :as delete-response} (delete-sow! api sow-id)
          find-response (find-sow-by-id api sow-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))

  (it "deletes all related project-sows when deleting an SOW"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id :project-id project-id})
          {api :api :as delete-response} (delete-sow! api sow-id)
          find-project-sow-response (find-project-sow-by-id api project-sow-id)]
      (should-respond-with-not-found find-project-sow-response api)))

  (it "responds with not found when attempting to delete a SOW that does not exist"
    (let [api (api-fn)
          delete-response (delete-sow! api "bad-id")]
      (should-respond-with-not-found delete-response api)))

  (it "finds all SOWs"
    (let [{api :api sow1-id :result} (create-sow! (api-fn) {:hourly-rate 200
                                                            :currency-code "USD"
                                                            :start one-day-from-now
                                                            :end two-days-from-now
                                                            :url "www.example.com"})
          {api :api sow1 :result} (find-sow-by-id api sow1-id)
          {api :api sow2-id :result} (create-sow! api {:hourly-rate 175
                                                       :currency-code "USD"
                                                       :start two-days-from-now
                                                       :end three-days-from-now
                                                       :url "www.examples.com"
                                                       :signed-date one-day-ago})
          {api :api sow2 :result} (find-sow-by-id api sow2-id)
          find-all-response (find-all-sows api {})]
      (should= :success (:status find-all-response))
      (should= api (:api find-all-response))
      (should== [sow1 sow2] (:result find-all-response))))

  (it "finds all SOWs for a project"
    (let [{api :api sow1-id :result} (create-sow! (api-fn) {:hourly-rate 200
                                                            :currency-code "USD"
                                                            :start one-day-from-now
                                                            :end two-days-from-now
                                                            :url "www.example.com"})
          {sow1 :result} (find-sow-by-id api sow1-id)
          {api :api sow2-id :result} (create-sow! api {:hourly-rate 175
                                                       :currency-code "USD"
                                                       :start two-days-from-now
                                                       :end three-days-from-now
                                                       :url "www.examples.com"
                                                       :signed-date one-day-ago})
          {sow2 :result} (find-sow-by-id api sow2-id)
          {api :api sow3-id :result} (create-sow! api {:hourly-rate 150
                                                       :currency-code "GBP"
                                                       :start one-day-from-now
                                                       :end three-days-from-now
                                                       :url "www.examples1.com"
                                                       :signed-date two-days-ago})
          {sow3 :result} (find-sow-by-id api sow3-id)
          {api :api project1-id :result} (create-project! api {:name "project 1"})
          {project1 :result} (find-project-by-id api project1-id)
          {api :api project2-id :result} (create-project! api {:name "project 2"})
          {api :api project-sow1-id :result} (create-project-sow! api {:project-id project1-id
                                                    :sow-id sow1-id})
          {api :api project-sow2-id :result} (create-project-sow! api {:project-id project2-id
                                                    :sow-id sow2-id})
          {api :api project-sow3-id :result} (create-project-sow! api {:project-id project1-id
                                                    :sow-id sow3-id})
          find-all-response (find-all-sows api {:project-id project1-id})]
      (should= :success (:status find-all-response))
      (should= api (:api find-all-response))
      (should== [sow1 sow3] (:result find-all-response))))

  (it "creates a project-sow"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          response (create-project-sow! api {:sow-id sow-id
                                             :project-id project-id})
          {api :api project-sow-id :result} response
          {created-project-sow :result} (find-project-sow-by-id api project-sow-id)]
      (should= :success (:status response))
      (should= sow-id (:sow-id created-project-sow))
      (should= project-id (:project-id created-project-sow))
      (should-have-create-timestamps created-project-sow)))

  (it "finds a project-sow by id"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:project-id project-id
                                                                           :sow-id sow-id})
          find-response (find-project-sow-by-id api project-sow-id)
          project-sow (:result find-response)]
      (should= :success (:status find-response))
      (should= project-sow-id (:id project-sow))
      (should= project-id (:project-id project-sow))
      (should= sow-id (:sow-id project-sow))
      (should-have-create-timestamps project-sow)
      (should= api (:api find-response))))

  (it "responds with not found when the project-sow does not exist"
    (let [api (api-fn)
          response1 (find-project-sow-by-id api "bad-id")
          response2 (find-project-sow-by-id api "10")]
      (should-respond-with-not-found response1 api)
      (should-respond-with-not-found response2 api)))

  (it "fails to create a project-sow when the sow-id is missing"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "test"})
          response (create-project-sow! api {:project-id project-id})]
      (should-fail-with-errors response api [response/project-sows-missing-sow-id])))

  (it "fails to create a project-sow when the sow-id is invalid"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "test"})
          response1 (create-project-sow! api {:sow-id "abc"
                                              :project-id project-id})
          response2 (create-project-sow! api {:sow-id "10"
                                              :project-id project-id})]
      (should-fail-with-errors response1 api [response/project-sows-invalid-sow-id])
      (should-fail-with-errors response2 api [response/project-sows-invalid-sow-id])))

  (it "fails to create a project-sow when the project-id is missing"
    (let [[api sow-id] (test-sow (api-fn))
          response (create-project-sow! api {:sow-id sow-id})]
      (should-fail-with-errors response api [response/project-sows-missing-project-id])))

  (it "fails to create a project-sow when the project-id is invalid"
    (let [[api sow-id] (test-sow (api-fn))
          response1 (create-project-sow! api {:sow-id sow-id
                                              :project-id "abc"})
          response2 (create-project-sow! api {:sow-id sow-id
                                              :project-id "10"})]
      (should-fail-with-errors response1 api [response/project-sows-invalid-project-id])
      (should-fail-with-errors response2 api [response/project-sows-invalid-project-id])))

  (it "updates a project-sow with a new project and SOW"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project1-id :result} (create-project! api {:name "test 1"})
          {api :api project2-id :result} (create-project! api {:name "test 2"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project1-id})
          {api :api} (update-project-sow! api project-sow-id {:project-id project2-id})
          find-response (find-project-sow-by-id api project-sow-id)
          {project-sow :result} find-response]
      (should= :success (:status find-response))
      (should= sow-id (:sow-id project-sow))
      (should= project2-id (:project-id project-sow))))

  (it "responds with not found when attempting to delete a project-sow that does not exist"
    (let [update-response1 (update-employment! (api-fn) "10" {})
          update-response2 (update-employment! (api-fn) "bad-id" {})]
      (should-respond-with-not-found update-response1 (api-fn))
      (should-respond-with-not-found update-response2 (api-fn))))

  (it "does nothing when given nothing to update"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project-id})
          {project-sow :result} (find-project-sow-by-id api project-sow-id)
          {api :api update-status :status} (update-project-sow! api project-sow-id {})
          {after-update :result} (find-project-sow-by-id api project-sow-id)]
      (should= :success update-status)
      (should= (:sow-id project-sow) (:sow-id after-update))
      (should= (:project-id project-sow) (:project-id after-update))))

  (it "fails to update a project-sow when the project id is nil"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project-id})
          response (update-project-sow! api project-sow-id {:project-id nil})]
      (should-fail-with-errors response api [response/project-sows-missing-project-id])))

  (it "fails to update a project-sow when the SOW id is nil"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project-id})
          response (update-project-sow! api project-sow-id {:sow-id nil})]
      (should-fail-with-errors response api [response/project-sows-missing-sow-id])))

  (it "fails to update an project-sow when project is invalid"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project-id})
          update-response1 (update-project-sow! api project-sow-id {:project-id "bad-id"})
          update-response2 (update-project-sow! api project-sow-id {:project-id "10"})]
      (should-fail-with-errors update-response1 api [response/project-sows-invalid-project-id])
      (should-fail-with-errors update-response2 api [response/project-sows-invalid-project-id])))

  (it "fails to update an project-sow when SOW is invalid"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                      :project-id project-id})
          update-response1 (update-project-sow! api project-sow-id {:sow-id "bad-id"})
          update-response2 (update-project-sow! api project-sow-id {:sow-id "10"})]
      (should-fail-with-errors update-response1 api [response/project-sows-invalid-sow-id])
      (should-fail-with-errors update-response2 api [response/project-sows-invalid-sow-id])))

  (it "finds all project-sows"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api sow :result} (find-sow-by-id api sow-id)
          {api :api project1-id :result} (create-project! api {:name "test1"})
          {api :api project2-id :result} (create-project! api {:name "test2"})
          {api :api project-sow-id-1 :result} (create-project-sow! api {:sow-id sow-id
                                                                        :project-id project1-id})
          {api :api project-sow-id-2 :result} (create-project-sow! api {:sow-id sow-id
                                                                        :project-id project2-id})
          {sow :result} (find-sow-by-id api sow-id)
          {project1 :result} (find-project-by-id api project1-id)
          {project2 :result} (find-project-by-id api project2-id)
          {project-sow-1 :result} (find-project-sow-by-id api project-sow-id-1)
          {project-sow-2 :result} (find-project-sow-by-id api project-sow-id-2)
          response (find-all-project-sows api)]
      (should= :success (:status response))
      (should== [project-sow-1 project-sow-2]
                (:result response))
      (should= api (:api response))))

  (it "deletes a project-sow"
    (let [[api sow-id] (test-sow (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api project-sow-id :result} (create-project-sow! api {:sow-id sow-id
                                                                   :project-id project-id})
          {api :api :as delete-response} (delete-project-sow! api project-sow-id)
          find-response (find-project-sow-by-id api project-sow-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))

  (it "deletes all project-sows for a given sow-id"
    (let [{api :api sow1-id :result} (create-sow! (api-fn) {:hourly-rate 200
                                                            :currency-code "USD"
                                                            :start one-day-from-now
                                                            :end two-days-from-now
                                                            :url "www.example.com"})
          {sow1 :result} (find-sow-by-id api sow1-id)
          {api :api sow2-id :result} (create-sow! api {:hourly-rate 175
                                                       :currency-code "USD"
                                                       :start two-days-from-now
                                                       :end three-days-from-now
                                                       :url "www.examples.com"
                                                       :signed-date one-day-ago})
          {sow2 :result} (find-sow-by-id api sow2-id)
          {api :api project-id1 :result} (create-project! api {:name "Project 1"})
          {api :api project-id2 :result} (create-project! api {:name "Project 2"})
          {project2 :result} (find-project-by-id api project-id2)
          {api :api project-sow1 :result} (create-project-sow! api {:project-id project-id1 :sow-id sow1-id})
          {api :api project-sow2 :result} (create-project-sow! api {:project-id project-id2 :sow-id sow2-id})
          {api :api project-sow3 :result} (create-project-sow! api {:project-id project-id2 :sow-id sow1-id})
          {api :api :as delete-response} (delete-project-sows-for-sow! api sow1-id)
          find-response1 (find-all-projects api {:sow-id sow1-id})
          find-response2 (find-all-projects api {:sow-id sow2-id})]
      (should= :success (:status delete-response))
      (should= [] (:result find-response1))
      (should= [project2] (:result find-response2))))

  (it "responds with not found when attempting to delete a project-sow that does not exist"
    (let [api (api-fn)
          delete-response (delete-project-sow! api "bad-id")]
      (should-respond-with-not-found delete-response api)))


  (it "creates a person"
    (let [{api :api person-id :result status :status} (create-person! (api-fn) {:first-name "John"
                                                                            :last-name "Smith"
                                                                            :email "john@example.com"})
          {api :api person :result} (find-person-by-id api person-id)]
      (should= :success status)
      (should= person-id (:id person))
      (should= "John" (:first-name person))
      (should= "Smith" (:last-name person))
      (should= "john@example.com" (:email person))
      (should-have-create-timestamps person)))

  (it "responds with not found when the person does not exist"
    (let [response1 (find-person-by-id (api-fn) "bad-id")
          response2 (find-person-by-id (api-fn) "10")]
      (should-respond-with-not-found response1 (api-fn))
      (should-respond-with-not-found response2 (api-fn))))

  (it "updates a person"
    (let [{api :api person-id :result status :status} (create-person! (api-fn) {:first-name "John"
                                                                            :last-name "Smith"
                                                                            :email "john@example.com"})
          {created-person :result} (find-person-by-id api person-id)
          {api :api :as response} (update-person! api person-id {:first-name "New John"
                                                                 :last-name "New Smith"
                                                                 :email "new@example.com"})
          {updated-person :result} (find-person-by-id api person-id)]
      (should= :success (:status response))
      (should= person-id (:id updated-person))
      (should= "New John" (:first-name updated-person))
      (should= "New Smith" (:last-name updated-person))
      (should= "new@example.com" (:email updated-person))))

  (it "responds with not found when updating a person that cannot be found"
    (let [response (update-person! (api-fn) "bad-id" {:first-name "New John"
                                                      :last-name "New Smith"
                                                      :email "new@example.com"})]
      (should-respond-with-not-found response (api-fn))))

  (it "finds all people"
    (let [{api :api person1-id :result} (create-person! (api-fn) {:first-name "John"})
          {api :api person1 :result} (find-person-by-id api person1-id)
          {api :api person2-id :result} (create-person! api {:first-name "Sally"})
          {api :api person2 :result} (find-person-by-id api person2-id)
          find-response (find-all-people api)]
      (should= :success (:status find-response))
      (should= api (:api find-response))
      (should== [person1 person2] (:result find-response))))

  (it "searches for a person by first name"
    (let [{api :api person1-id :result} (create-person! (api-fn) {:first-name "John"})
          {api :api person2-id :result} (create-person! api {:first-name "John"})
          {api :api} (create-person! api {:first-name "Bob"})
          {api :api people :result} (search-people api {:first-name "John"})]
      (should= 2 (count people))
      (should== [person1-id person2-id] (map #(:id %) people))))

  (it "searches for a person by last name"
    (let [{api :api person1-id :result status :status} (create-person! (api-fn) {:last-name "Smith"})
          {api :api person2-id :result status :status} (create-person! api {:last-name "Smith"})
          {api :api} (create-person! api {:last-name "Jones"})
          {api :api people :result} (search-people api {:last-name "Smith"})]
      (should= 2 (count people))
      (should== [person1-id person2-id] (map #(:id %) people))))

  (it "searches for a person by email"
    (let [{api :api person1-id :result status :status} (create-person! (api-fn) {:email "fake1@null.com"})
          {api :api} (create-person! api {:email "fake2@null.com"})
          {api :api people :result} (search-people api {:email "fake1@null.com"})]
      (should= 1 (count people))
      (should= [person1-id] (map #(:id %) people))))

  (it "searches by all three person attributes"
    (let [{api :api person1-id :result status :status} (create-person! (api-fn) {:first-name "Bob"
                                                                                 :last-name "Smith"
                                                                                 :email "fake1@null.com"})
          {api :api} (create-person! api {:first-name "Sally"
                                          :last-name "Smith"
                                          :email "fake2@null.com"})
          {api :api people :result} (search-people api {:first-name "Bob"
                                                        :last-name "Smith"
                                                        :email "fake1@null.com"})]
      (should= 1 (count people))
      (should= [person1-id] (map #(:id %) people))))

  (it "creates employment positions"
    (let [create-response (create-employment-position! (api-fn) {:name "Craftsman"})
          {api :api position-id :result} create-response
          find-response (find-all-employment-positions api)
          created-position (first (:result find-response))]
      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should= api (:api find-response))
      (should= "Craftsman" (:name created-position))
      (should= position-id (:id created-position))
      (should-have-create-timestamps created-position)))

  (it "creates an employment record with a person, start date, and position"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start ten-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-id position-id
                                                   :location-id location-id
                                                   :start start})
          {api :api employment-id :result} create-response
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= :success (:status create-response))
      (should= :success (:status find-response))
      (should= person-id (:person-id employment))
      (should= position-id (:position-id employment))
      (should= start (:start employment))))

  (it "creates an employment record with a person, start date, position and end date"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-id position-id
                                                   :location-id location-id
                                                   :start start
                                                   :end end})
          {api :api employment-id :result} create-response
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= person-id (:person-id employment))
      (should= position-id (:position-id employment))
      (should= start (:start employment))
      (should= end (:end employment))))

  (it "creates an employment record when given a position name and no position id"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-name "test"
                                                   :location-id location-id
                                                   :start start
                                                   :end end})
          {api :api employment-id :result} create-response
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= person-id (:person-id employment))
      (should= position-id (:position-id employment))
      (should= start (:start employment))
      (should= end (:end employment))))

  (it "creates an employment record when given a position name and position id that match"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-name "test"
                                                   :position-id position-id
                                                   :location-id location-id
                                                   :start start
                                                   :end end})
          {api :api employment-id :result} create-response
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= person-id (:person-id employment))
      (should= position-id (:position-id employment))
      (should= start (:start employment))
      (should= end (:end employment))))

  (it "fails to create an employment when person and position are missing"
    (let [{api :api location-id :result} (create-location! (api-fn) {:name "chicago"})
          create-response (create-employment! api {:start three-days-ago
                                                   :location-id location-id})]
      (should-fail-with-errors create-response api [response/employment-missing-person-id
                                                    response/employment-missing-position-id])))

  (it "fails to create an employment when the person id is missing"
    (let [{api :api position-id :result} (create-employment-position! (api-fn) {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          create-response (create-employment! api {:start ten-days-ago
                                                   :position-id position-id
                                                   :location-id location-id})]
      (should-fail-with-errors create-response api [response/employment-missing-person-id])))

  (it "fails to create an employment when the position id is missing"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          create-response (create-employment! api {:start three-days-ago
                                                   :person-id person-id
                                                   :location-id location-id})]
      (should-fail-with-errors create-response api [response/employment-missing-position-id])))

  (it "fails to create an employment when the start date is missing"
    (let [{api :api position-id :result} (create-employment-position! (api-fn) {:name "test"})
          {api :api person-id :result} (create-person! api {:first-name "a"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          create-response (create-employment! api {:position-id position-id
                                                   :person-id person-id
                                                   :location-id location-id})]
      (should-fail-with-errors create-response api [response/employment-missing-start-date])))

  (it "fails to create an employment record when location id is missing"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          start ten-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-id position-id
                                                   :start start})]
      (should-fail-with-errors create-response api [response/employment-data-missing-location-id])))

  (it "fails to create an employment when location is invalid"
    (let [{api :api position-id :result} (create-employment-position! (api-fn) {:name "test"})
          {api :api person-id :result} (create-person! api {:first-name "a"})
          create-response1 (create-employment! api {:start ten-days-ago
                                                    :person-id person-id
                                                    :position-id position-id
                                                    :location-id "bad-id"})
          create-response2 (create-employment! api {:start ten-days-ago
                                                    :person-id person-id
                                                    :position-id position-id
                                                    :location-id "10"})]
      (should-fail-with-errors create-response1 api [response/location-memberships-invalid-location-id])
      (should-fail-with-errors create-response2 api [response/location-memberships-invalid-location-id])))

  (it "fails to create an employment when person is invalid"
    (let [{api :api position-id :result} (create-employment-position! (api-fn) {:name "test"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          create-response1 (create-employment! api {:start ten-days-ago
                                                    :person-id "bad-id"
                                                    :position-id position-id
                                                    :location-id location-id})
          create-response2 (create-employment! api {:start ten-days-ago
                                                    :person-id "10"
                                                    :position-id position-id
                                                    :location-id location-id})]
      (should-fail-with-errors create-response1 api [response/employment-invalid-person-id])
      (should-fail-with-errors create-response2 api [response/employment-invalid-person-id])))

  (it "fails to create an employment when position is invalid"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          create-response1 (create-employment! api {:start ten-days-ago
                                                    :person-id person-id
                                                    :position-id "bad-id"
                                                    :location-id location-id})
          create-response2 (create-employment! api {:start ten-days-ago
                                                    :person-id person-id
                                                    :position-id "10"
                                                    :location-id location-id})]
      (should-fail-with-errors create-response1 api [response/employment-invalid-position-id])
      (should-fail-with-errors create-response2 api [response/employment-invalid-position-id])))

  (it "fails to create an employment record when given an invalid position name"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test1"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-name "test2"
                                                   :location-id location-id
                                                   :start start
                                                   :end end})]
      (should-fail-with-errors create-response api [response/employment-invalid-position-name])))

  (it "fails to create an employment record when given an invalid position name but a valid position id"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id :result} (create-employment-position! api {:name "test1"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-name "test2"
                                                   :position-id position-id
                                                   :location-id location-id
                                                   :start start
                                                   :end end})]
      (should-fail-with-errors create-response api [response/employment-invalid-position-name])))

  (it "fails to create an employment record when given a position name and position id that do not match"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api position-id-1 :result} (create-employment-position! api {:name "test1"})
          {api :api position-id-2 :result} (create-employment-position! api {:name "test2"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start four-days-ago
          end three-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-name "test2"
                                                   :position-id position-id-1
                                                   :location-id location-id
                                                   :start start
                                                   :end end})]
      (should-fail-with-errors create-response api [response/employment-position-mismatch])))

  (it "updates an employment record with a new person, start date, position and end date"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api person-id2 :result} (create-person! api {:first-name "b"})
          {api :api position-id2 :result} (create-employment-position! api {:name "test1"})
          {api :api} (update-employment! api employment-id {:person-id person-id2
                                                            :position-id position-id2
                                                            :start five-days-ago
                                                            :end ten-days-ago})
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= :success (:status find-response))
      (should= person-id2 (:person-id employment))
      (should= position-id2 (:position-id employment))
      (should= five-days-ago (:start employment))
      (should= ten-days-ago (:end employment))))

  (it "responds with not found when the employment does not exist"
    (let [update-response1 (update-employment! (api-fn) "10" {})
          update-response2 (update-employment! (api-fn) "bad-id" {})]
      (should-respond-with-not-found update-response1 (api-fn))
      (should-respond-with-not-found update-response2 (api-fn))))

  (it "can update only one field"
    (let [[api employment-id] (test-employment (api-fn))
          {employment :result} (find-employment-by-id api employment-id)
          {api :api update-status :status} (update-employment! api employment-id {:start five-days-ago})
          {after-update :result} (find-employment-by-id api employment-id)]
      (should= :success update-status)
      (should= (:person-id employment) (:person-id after-update))
      (should= (:position-id employment) (:position-id after-update))
      (should= five-days-ago (:start after-update))
      (should= five-days-ago (:end after-update))))

  (it "does nothing when given nothing to update"
    (let [[api employment-id] (test-employment (api-fn))
          {employment :result} (find-employment-by-id api employment-id)
          {api :api update-status :status} (update-employment! api employment-id {})
          {after-update :result} (find-employment-by-id api employment-id)]
      (should= :success update-status)
      (should= (:person-id employment) (:person-id after-update))
      (should= (:position-id employment) (:position-id after-update))
      (should= (:start employment) (:start after-update))
      (should= (:end employment) (:end after-update))))

  (it "fails to update an employment when the person id is nil"
    (let [[api employment-id] (test-employment (api-fn))
          response (update-employment! api employment-id {:person-id nil})]
      (should-fail-with-errors response api [response/employment-missing-person-id])))

  (it "fails to update an employment when the position id is nil"
    (let [[api employment-id] (test-employment (api-fn))
          response (update-employment! api employment-id {:position-id nil})]
      (should-fail-with-errors response api [response/employment-missing-position-id])))

  (it "fails to update an employment when person is invalid"
    (let [[api employment-id] (test-employment (api-fn))
          update-response1 (update-employment! api employment-id {:person-id "bad-id"})
          update-response2 (update-employment! api employment-id {:person-id "10"})]
      (should-fail-with-errors update-response1 api [response/employment-invalid-person-id])
      (should-fail-with-errors update-response2 api [response/employment-invalid-person-id])))

  (it "fails to update an employment when person is invalid"
    (let [[api employment-id] (test-employment (api-fn))
          update-response1 (update-employment! api employment-id {:position-id "bad-id"})
          update-response2 (update-employment! api employment-id {:position-id "10"})]
      (should-fail-with-errors update-response1 api [response/employment-invalid-position-id])
      (should-fail-with-errors update-response2 api [response/employment-invalid-position-id])))

  (defn test-employments [api options]
    (let [{api :api developer-id :result} (create-employment-position! api {:name "Developer"})
          {api :api developer :result} (find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (create-employment-position! api {:name "Admin"})
          {api :api admin :result} (find-employment-position-by-id api admin-id)
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (find-person-by-id api john-id)
          {api :api sally-id :result} (create-person! api {:first-name "John" :last-name "Jones"})
          {api :api sally :result} (find-person-by-id api sally-id)
          {api :api location1-id :result} (create-location! api {:name "chicago"})
          {api :api location2-id :result} (create-location! api {:name "london"})
          {api :api employment1-id :result} (create-employment! api {:person-id john-id
                                                                     :position-id developer-id
                                                                     :location-id location1-id
                                                                     :start ten-days-ago
                                                                     :end four-days-ago})
          {api :api employment1 :result} (find-employment-by-id api employment1-id)
          {api :api employment2-id :result} (create-employment! api {:person-id sally-id
                                                                     :position-id admin-id
                                                                     :location-id location2-id
                                                                     :start five-days-ago
                                                                     :end three-days-ago})
          {api :api employment2 :result} (find-employment-by-id api employment2-id)
          {location-membership1 :result} (find-all-location-memberships-for-employment api employment1-id)
          {location-membership2 :result} (find-all-location-memberships-for-employment api employment2-id)]
      {:api api
       :position1 developer
       :position2 admin
       :person1 john
       :person2 sally
       :employment1 employment1
       :employment1-id employment1-id
       :employment2 employment2
       :employment2-id employment2-id
       :location1-id location1-id
       :location2-id location2-id
       :location-membership1 location-membership1
       :location-membership2 location-membership2}))

  (it "finds all employments and includes the associated person and position"
    (let [{api :api :as test-data} (test-employments (api-fn) {})
          response (find-all-employments api {})]
      (should= :success (:status response))
      (should== [(assoc (:employment1 test-data) :person (:person1 test-data) :position (:position1 test-data))
                 (assoc (:employment2 test-data) :person (:person2 test-data) :position (:position2 test-data))]
                (:result response))))

  (defn position-names [response]
    (->> response
      :result
      (map #(-> % :position :name))))

  (it "finds all employments and sorts by position name"
    (let [{api :api :as test-data} (test-employments (api-fn) {})]
      (should= ["Admin" "Developer"]
               (position-names (find-all-employments api {:sort :position :direction :asc})))
      (should= ["Developer" "Admin"]
               (position-names (find-all-employments api {:sort :position :direction :desc})))))

  (defn last-names [response]
    (->> response
      :result
      (map #(-> % :person :last-name))))

  (it "finds all employments and sorts by first name then last name"
    (let [{api :api :as test-data} (test-employments (api-fn) {})]
      (should= ["Jones" "Smith"]
               (last-names (find-all-employments api {:sort :full-name :direction :asc})))
      (should= ["Smith" "Jones"]
               (last-names (find-all-employments api {:sort :full-name :direction :desc})))))

  (it "finds all location memberships for an employment"
    (let [{api :api :as test-data} (test-employments (api-fn) {})
          location1-id (:location1-id test-data)
          location2-id (:location2-id test-data)
          {api :api location-membership3-id :result} (create-location-membership! api (:employment2-id test-data) location1-id (create-valid-location-membership-data))
          {location-membership3 :result} (find-location-membership-by-id api location-membership3-id)
          location1-find-all-response (find-all-location-memberships-for-employment api (:employment1-id test-data))
          location2-find-all-response (find-all-location-memberships-for-employment api (:employment2-id test-data))]
      (should== (flatten [(:location-membership1 test-data)]) (:result location1-find-all-response))
      (should= :success (:status location1-find-all-response))
      (should= api (:api location1-find-all-response))
      (should= (flatten [(:location-membership2 test-data) location-membership3]) (:result location2-find-all-response))))

  (it "finds all employments for location"
    (let [{api :api :as test-data} (test-employments (api-fn) {})
          location1-id (:location1-id test-data)
          location2-id (:location2-id test-data)
          {api :api} (create-location-membership! api (:employment2-id test-data) location1-id (create-valid-location-membership-data))
          location1-response (find-all-employments api {:location-id location1-id})
          location2-response (find-all-employments api {:location-id location2-id})]
      (should== [(assoc (:employment1 test-data) :person (:person1 test-data) :position (:position1 test-data))
                 (assoc (:employment2 test-data) :person (:person2 test-data) :position (:position2 test-data))]
                (:result location1-response))
      (should== [(assoc (:employment2 test-data) :person (:person2 test-data) :position (:position2 test-data))]
                (:result location2-response))
      (should= :success (:status location1-response))
      (should= :success (:status location2-response))
      (should= api (:api location1-response))
      (should= api (:api location2-response))))

  (it "does not return duplicate employments for the same position and location"
    (let [{api :api position-id :result} (create-employment-position! (api-fn) {:name "Developer"})
          {api :api position :result} (find-employment-position-by-id api position-id)
          {api :api person1-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api person1 :result} (find-person-by-id api person1-id)
          {api :api person2-id :result} (create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api person2 :result} (find-person-by-id api person2-id)
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api employment1-id :result} (create-employment! api {:person-id person1-id
                                                                     :position-id position-id
                                                                     :location-id location-id
                                                                     :start five-days-ago
                                                                     :end one-day-from-now})
          {api :api employment1 :result} (find-employment-by-id api employment1-id)
          {api :api employment2-id :result} (create-employment! api {:person-id person2-id
                                                                     :position-id position-id
                                                                     :location-id location-id
                                                                     :start three-days-ago})
          {api :api employment2 :result} (find-employment-by-id api employment2-id)
          {api :api location-membership3-id :result} (create-location-membership! api employment1-id location-id (:start one-day-ago))
          location-response (find-all-employments api {:location-id location-id})]
      (should== [(assoc employment2 :person person2 :position position)
                 (assoc employment1 :person person1 :position position)]
                (:result location-response))
      (should= :success (:status location-response))
      (should= api (:api location-response))))

(it "filters out employments that ended before the start date"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api resident-id :result} (create-employment-position! (api-fn) {:name "Resident"})
          {api :api resident :result} (find-employment-position-by-id api resident-id)
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (find-person-by-id api john-id)
          {api :api ends-before-start-date-id :result} (create-employment! api {:person-id john-id
                                                                                :position-id resident-id
                                                                                :start ten-days-ago
                                                                                :end four-days-ago})
          {api :api ends-before-start-date :result} (find-employment-by-id api ends-before-start-date-id)
          response (find-all-employments api {:start-date start-date :end-date end-date})]
      (should== [] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "filters out employments that start after the end date"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api admin-id :result} (create-employment-position! (api-fn) {:name "Admin"})
          {api :api admin :result} (find-employment-position-by-id api admin-id)
          {api :api sally-id :result} (create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (find-person-by-id api sally-id)
          {api :api starts-after-end-date-id :result} (create-employment! api {:person-id sally-id
                                                                               :position-id admin-id
                                                                               :start five-days-from-now
                                                                               :end ten-days-from-now})
          {api :api starts-after-end-date :result} (find-employment-by-id api starts-after-end-date-id)
          response (find-all-employments api {:start-date start-date :end-date end-date})]
      (should== [] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "finds all employments for a date range"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api resident-id :result} (create-employment-position! (api-fn) {:name "Resident"})
          {api :api resident :result} (find-employment-position-by-id api resident-id)
          {api :api developer-id :result} (create-employment-position! api {:name "Developer"})
          {api :api developer :result} (find-employment-position-by-id api developer-id)
          {api :api admin-id :result} (create-employment-position! api {:name "Admin"})
          {api :api admin :result} (find-employment-position-by-id api admin-id)
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (find-person-by-id api john-id)
          {api :api sally-id :result} (create-person! api {:first-name "Sally" :last-name "Jones"})
          {api :api sally :result} (find-person-by-id api sally-id)
          {api :api chicago-id :result} (create-location! api {:name "Chicago"})
          {api :api ends-before-start-date-id :result} (create-employment! api {:person-id john-id
                                                                                :position-id resident-id
                                                                                :start ten-days-ago
                                                                                :end four-days-ago
                                                                                :location-id chicago-id})
          {api :api ends-before-start-date :result} (find-employment-by-id api ends-before-start-date-id)
          {api :api starts-after-end-date-id :result} (create-employment! api {:person-id sally-id
                                                                               :position-id admin-id
                                                                               :start five-days-from-now
                                                                               :end ten-days-from-now
                                                                               :location-id chicago-id})
          {api :api starts-after-end-date :result} (find-employment-by-id api starts-after-end-date-id)
          {api :api within-date-range-id :result} (create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :end ten-days-from-now
                                                                           :location-id chicago-id})
          {api :api within-date-range :result} (find-employment-by-id api within-date-range-id)
          response (find-all-employments api {:start-date start-date :end-date end-date})]
      (should== [(assoc within-date-range :person john :position developer)] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "does not return a current employment for a date range and location-id if the location-membership ends before the start date"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api developer-id :result} (create-employment-position! (api-fn) {:name "Developer"})
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api within-date-range-id :result}
            (create-employment! api {:person-id john-id
                                     :position-id developer-id
                                     :start ten-days-ago
                                     :end ten-days-from-now})
          {api :api within-date-range :result} (find-employment-by-id api within-date-range-id)
          {api :api chicago-id :result} (create-location! api {:name "Chicago"})
          {api :api chicago :result} (find-location-by-id api chicago-id)
          {api :api london-id :result} (create-location! api {:name "London"})
          {api :api london :result} (find-location-by-id api london-id)
          {api :api chicago-location-membership-id :result} (create-location-membership! api within-date-range-id chicago-id {:start ten-days-ago})
          {api :api london-location-membership-id :result} (create-location-membership! api within-date-range-id london-id {:start start-date})
          response (find-all-employments api {:start-date start-date :end-date end-date :location-id chicago-id})]
      (should== [] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "does not return a current employment for a date range and location-id if the location-membership begins after the end date"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api developer-id :result} (create-employment-position! (api-fn) {:name "Devs;dkfjas;dlfjeloper"})
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api within-date-range-id :result} (create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :end ten-days-from-now})
          {api :api within-date-range :result} (find-employment-by-id api within-date-range-id)
          {api :api chicago-id :result} (create-location! api {:name "Chicago"})
          {api :api chicago :result} (find-location-by-id api chicago-id)
          {api :api london-id :result} (create-location! api {:name "London"})
          {api :api london :result} (find-location-by-id api london-id)
          {api :api chicago-location-membership-id :result} (create-location-membership! api within-date-range-id chicago-id {:start ten-days-ago})
          {api :api london-location-membership-id :result} (create-location-membership! api within-date-range-id london-id {:start five-days-from-now})
          response (find-all-employments api {:start-date start-date :end-date end-date :location-id london-id})]
      (should== [] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "finds all employments for a date range and a location-id"
    (let [start-date three-days-ago
          end-date three-days-from-now
         {api :api resident-id :result} (create-employment-position! (api-fn) {:name "Resident"})
         {api :api resident :result} (find-employment-position-by-id api resident-id)
         {api :api developer-id :result} (create-employment-position! api {:name "Developer"})
         {api :api developer :result} (find-employment-position-by-id api developer-id)
         {api :api admin-id :result} (create-employment-position! api {:name "Admin"})
         {api :api admin :result} (find-employment-position-by-id api admin-id)
         {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
         {api :api john :result} (find-person-by-id api john-id)
         {api :api sally-id :result} (create-person! api {:first-name "Sally" :last-name "Jones"})
         {api :api sally :result} (find-person-by-id api sally-id)
         {api :api jane-id :result} (create-person! api {:first-name "Jane" :last-name "Doe"})
         {api :api jane :result} (find-person-by-id api jane-id)
         {api :api chicago-id :result} (create-location! api {:name "Chicago"})
         {api :api london-id :result} (create-location! api {:name "London"})
         {api :api ends-before-start-date-id :result}
           (create-employment! api {:person-id john-id
                                    :position-id resident-id
                                    :start ten-days-ago
                                    :end four-days-ago
                                    :location-id chicago-id})
         {api :api ends-before-start-date :result}
           (find-employment-by-id api ends-before-start-date-id)
         {api :api starts-after-end-date-id :result}
           (create-employment! api {:person-id sally-id
                                    :position-id admin-id
                                    :start five-days-from-now
                                    :end ten-days-from-now
                                    :location-id chicago-id})
         {api :api starts-after-end-date :result}
           (find-employment-by-id api starts-after-end-date-id)
         {api :api within-date-range-moved-to-london-id :result}
           (create-employment! api {:person-id john-id
                                    :position-id developer-id
                                    :start ten-days-ago
                                    :end ten-days-from-now
                                    :location-id chicago-id})
         {api :api within-date-range-moved-to-london :result}
           (find-employment-by-id api within-date-range-moved-to-london-id)
         {api :api within-date-range-currently-in-chicago-id :result}
           (create-employment! api {:person-id jane-id
                                    :position-id developer-id
                                    :start ten-days-ago
                                    :end ten-days-from-now
                                    :location-id chicago-id})
         {api :api within-date-range-currently-in-chicago :result}
           (find-employment-by-id api within-date-range-currently-in-chicago-id)
         {api :api moved-to-london-id :result}
           (create-location-membership! api within-date-range-moved-to-london-id london-id {:start five-days-ago})
         response (find-all-employments api {:start-date start-date :end-date end-date :location-id chicago-id})]
      (should== [(assoc within-date-range-currently-in-chicago :person jane :position developer)]
               (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

(it "returns an employment for a date range and location-id when the employment end is null"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api developer-id :result} (create-employment-position! (api-fn) {:name "Devs;dkfjas;dlfjeloper"})
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (find-person-by-id api john-id)
          {api :api developer :result} (find-employment-position-by-id api developer-id)
          {api :api chicago-id :result} (create-location! api {:name "Chicago"})
          {api :api within-date-range-id :result} (create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :location-id chicago-id})
          {api :api within-date-range :result} (find-employment-by-id api within-date-range-id)
          response (find-all-employments api {:start-date start-date :end-date end-date :location-id chicago-id})]
      (should== [(assoc within-date-range :person john :position developer)] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

(it "returns an employment for a date range and location-id when the location-id is a string"
    (let [start-date three-days-ago
          end-date three-days-from-now
          {api :api developer-id :result} (create-employment-position! (api-fn) {:name "Devs;dkfjas;dlfjeloper"})
          {api :api john-id :result} (create-person! api {:first-name "John" :last-name "Smith"})
          {api :api john :result} (find-person-by-id api john-id)
          {api :api developer :result} (find-employment-position-by-id api developer-id)
          {api :api chicago-id :result} (create-location! api {:name "Chicago"})
          {api :api within-date-range-id :result} (create-employment! api {:person-id john-id
                                                                           :position-id developer-id
                                                                           :start ten-days-ago
                                                                           :location-id (str chicago-id)})
          {api :api within-date-range :result} (find-employment-by-id api within-date-range-id)
          response (find-all-employments api {:start-date start-date :end-date end-date :location-id chicago-id})]
      (should== [(assoc within-date-range :person john :position developer)] (:result response))
      (should= :success (:status response))
      (should= api (:api response))))

  (it "finds employment positions by name"
    (let [{api :api developer-position :position1} (test-employments (api-fn) {})
          {position :result} (find-employment-position-by-name api "Developer")]
      (should= developer-position position)))

  (it "returns nil if no employment position is found for a given name"
    (let [{api :api developer-position :position1} (test-employments (api-fn) {})
          {position :result} (find-employment-position-by-name api "Hacker")]
      (should= nil position)))

  (defn start-dates [response]
    (->> response
      :result
      (map :start)))

  (it "finds all employments and sorts by start date"
    (let [{api :api :as test-data} (test-employments (api-fn) {})]
      (should= [ten-days-ago five-days-ago]
               (start-dates (find-all-employments api {:sort :start :direction :asc})))
      (should= [five-days-ago ten-days-ago]
               (start-dates (find-all-employments api {:sort :start :direction :desc})))))

  (defn end-dates [response]
    (->> response
      :result
      (map :end)))

  (it "finds all employments and sorts by end date"
    (let [{api :api :as test-data} (test-employments (api-fn) {})]
      (should= [four-days-ago three-days-ago]
               (end-dates (find-all-employments api {:sort :end :direction :asc})))
      (should= [three-days-ago four-days-ago]
               (end-dates (find-all-employments api {:sort :end :direction :desc})))))

  (it "finds employment by id including relevant person and position"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "a"})
          {api :api person :result} (find-person-by-id api person-id)
          {api :api position-id :result} (create-employment-position! api {:name "test"})
          {api :api position :result} (find-employment-position-by-id api position-id)
          {api :api location-id :result} (create-location! api {:name "chicago"})
          start ten-days-ago
          create-response (create-employment! api {:person-id person-id
                                                   :position-id position-id
                                                   :location-id location-id
                                                   :start start})
          {api :api employment-id :result} create-response
          find-response (find-employment-by-id api employment-id)
          {employment :result} find-response]
      (should= person (:person employment))
      (should= position (:position employment))))

  (it "creates an engagement"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          response (create-engagement! api {:employment-id employment-id
                                            :project-id project-id
                                            :start ten-days-ago
                                            :end five-days-ago
                                            :confidence-percentage 90})
          {api :api engagement-id :result} response
          {created-engagement :result} (find-engagement-by-id api engagement-id)]
      (should= :success (:status response))
      (should= employment-id (:employment-id created-engagement))
      (should= project-id (:project-id created-engagement))
      (should= ten-days-ago (:start created-engagement))
      (should= five-days-ago (:end created-engagement))
      (should= 90 (:confidence-percentage created-engagement))
      (should-have-create-timestamps created-engagement)))

  (it "assigns a confidence percentage of 100 if none is given"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          response (create-engagement! api {:employment-id employment-id
                                            :project-id project-id
                                            :start ten-days-ago
                                            :end five-days-ago})
          {api :api engagement-id :result} response
          {created-engagement :result} (find-engagement-by-id api engagement-id)]
      (should= :success (:status response))
      (should= 100 (:confidence-percentage created-engagement))))

  (it "responds with not found when the engagement does not exist"
    (let [api (api-fn)
          response1 (find-engagement-by-id api "bad-id")
          response2 (find-engagement-by-id api "10")]
      (should-respond-with-not-found response1 api)
      (should-respond-with-not-found response2 api)))

  (it "fails to create an engagement when the start date is missing"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          response (create-engagement! api {:employment-id employment-id
                                            :project-id project-id
                                            :end five-days-ago})]
      (should-fail-with-errors response api [response/engagements-missing-start-date])))

  (it "fails to create an engagement when the end date is missing"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          response (create-engagement! api {:employment-id employment-id
                                            :project-id project-id
                                            :start five-days-ago})]
      (should-fail-with-errors response api [response/engagements-missing-end-date])))

  (it "fails to create an engagement when the employment-id is missing"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "test"})
          response (create-engagement! api {:project-id project-id
                                            :start ten-days-ago
                                            :end five-days-ago})]
      (should-fail-with-errors response api [response/engagements-missing-employment-id])))

  (it "fails to create an engagement when the employment-id is invalid"
    (let [{api :api project-id :result} (create-project! (api-fn) {:name "test"})
          response1 (create-engagement! api {:employment-id "abc"
                                            :project-id project-id
                                            :start ten-days-ago
                                            :end five-days-ago})
          response2 (create-engagement! api {:employment-id "10"
                                            :project-id project-id
                                            :start ten-days-ago
                                            :end five-days-ago})]
      (should-fail-with-errors response1 api [response/engagements-invalid-employment-id])
      (should-fail-with-errors response2 api [response/engagements-invalid-employment-id])))

  (it "fails to create an engagement when the project-id is missing"
    (let [[api employment-id] (test-employment (api-fn))
          response (create-engagement! api {:employment-id employment-id
                                            :start ten-days-ago
                                            :end five-days-ago})]
      (should-fail-with-errors response api [response/engagements-missing-project-id])))

  (it "fails to create an engagement when the project-id is invalid"
    (let [[api employment-id] (test-employment (api-fn))
          response1 (create-engagement! api {:employment-id employment-id
                                             :project-id "abc"
                                             :start ten-days-ago
                                             :end five-days-ago})
          response2 (create-engagement! api {:employment-id employment-id
                                             :project-id "10"
                                             :start ten-days-ago
                                             :end five-days-ago})]
      (should-fail-with-errors response1 api [response/engagements-invalid-project-id])
      (should-fail-with-errors response2 api [response/engagements-invalid-project-id])))

  (it "updates an engagement"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id :result} (create-engagement! api {:employment-id employment-id
                                                                    :project-id project-id
                                                                    :confidence-percentage 35
                                                                    :start ten-days-ago
                                                                    :end five-days-ago})
          {api :api status :status} (update-engagement! api engagement-id {:start five-days-ago
                                                                           :end ten-days-ago})
          {updated-engagement :result} (find-engagement-by-id api engagement-id)]
      (should= :success status)
      (should= employment-id (:employment-id updated-engagement))
      (should= project-id (:project-id updated-engagement))
      (should= 35 (:confidence-percentage updated-engagement))
      (should= five-days-ago (:start updated-engagement))
      (should= ten-days-ago (:end updated-engagement))))

  (it "responds with not found if the engagement does not exist"
    (let [api (api-fn)
          response (update-engagement! api "bad-id" {:start five-days-ago :end ten-days-ago})]
      (should-respond-with-not-found response api)))

  (it "validates the engagement on update"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id :result} (create-engagement! api {:employment-id employment-id
                                                                    :project-id project-id
                                                                    :start ten-days-ago
                                                                    :end five-days-ago})
          response (update-engagement! api engagement-id {:employment-id nil
                                                          :start five-days-ago
                                                          :end ten-days-ago})]
      (should-fail-with-errors response api [response/engagements-missing-employment-id])))

  (it "deletes an engagement"
    (let [[api engagement-id] (test-engagement (api-fn))
          {api :api :as delete-response} (delete-engagement! api engagement-id)
          find-response (find-engagement-by-id api engagement-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))

  (it "responds with not found when engagement does not exist"
    (let [api (api-fn)
          delete-response (delete-engagement! api "bad-id")]
      (should-respond-with-not-found delete-response api)))

  ;(it "fails to create an engagement when start is outside the employment date range")
  ;(it "fails to create an engagement when end is outside the employment date range")
  ;(it "fails to create an engagement when start is outside the employment date range")
  ;(it "fails to create an engagement when start is before end")
  ;(it "fails to create an engagement when start is equal to end")

  (it "finds all engagements"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "jabroni" :last-name "lee chin feman"})
          [api employment-id] (test-employment api {:person-id person-id})
          {api :api project-id :result} (create-project! api {:name "test1"})
          {api :api engagement-id-1 :result} (create-engagement! api {:employment-id employment-id
                                                                      :project-id project-id
                                                                      :start twelve-days-ago
                                                                      :end three-days-ago})
          {api :api engagement-id-2 :result} (create-engagement! api {:employment-id employment-id
                                                                      :project-id project-id
                                                                      :start ten-days-ago
                                                                      :end five-days-ago})
          {person :result} (find-person-by-id api person-id)
          {project :result} (find-project-by-id api project-id)
          {engagement-1 :result} (find-engagement-by-id api engagement-id-1)
          {engagement-2 :result} (find-engagement-by-id api engagement-id-2)
          response (find-all-engagements api {})]
      (should= :success (:status response))
      (should== [(assoc engagement-1
                        :person person
                        :project project)
                 (assoc engagement-2
                        :person person
                        :project project)]
                (:result response))
      (should= api (:api response))))

  (it "finds all engagements for the given project"
    (let [{api :api person-id :result} (create-person! (api-fn) {:first-name "jabroni" :last-name "lee chin feman"})
          [api employment-id] (test-employment api {:person-id person-id})
          {api :api project-id-1 :result} (create-project! api {:name "test1"})
          {api :api project-id-2 :result} (create-project! api {:name "test2"})
          {api :api engagement-id-1 :result} (create-engagement! api {:employment-id employment-id
                                                                      :project-id project-id-1
                                                                      :start twelve-days-ago
                                                                      :end three-days-ago})
          {api :api engagement-id-2 :result} (create-engagement! api {:employment-id employment-id
                                                                      :project-id project-id-1
                                                                      :start twelve-days-ago
                                                                      :end three-days-ago})
          {api :api} (create-engagement! api {:employment-id employment-id
                                              :project-id project-id-2
                                              :start twelve-days-ago
                                              :end three-days-ago})
          {person :result} (find-person-by-id api person-id)
          {project-1 :result} (find-project-by-id api project-id-1)
          {engagement-1 :result} (find-engagement-by-id api engagement-id-1)
          {engagement-2 :result} (find-engagement-by-id api engagement-id-2)
          {found-engagements :result} (find-all-engagements api {:project-id project-id-1})]
      (should= 2 (count found-engagements))
      (should-contain (assoc engagement-1
                             :person person
                             :project project-1)
                      found-engagements)
      (should-contain (assoc engagement-2
                             :person person
                             :project project-1)
                      found-engagements)))

  (defn engagement-ids [response]
    (map :id (:result response)))

  (it "finds an engagement that started before the given range but ends inside the given range"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id1 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start ten-days-ago
                                                                     :end three-days-ago})
          {api :api engagement-id2 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start ten-days-ago
                                                                     :end six-days-ago})
          found-ids (engagement-ids (find-all-engagements api {:start five-days-ago :end two-days-ago}))]
      (should= [engagement-id1] found-ids)))

  (it "finds an engagement that starts in the range but ends outside of the range"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id1 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start four-days-ago
                                                                     :end one-day-ago})
          {api :api engagement-id2 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start twelve-days-ago
                                                                     :end six-days-ago})
          found-ids (engagement-ids (find-all-engagements api {:start five-days-ago :end two-days-ago}))]
      (should= [engagement-id1] found-ids)))

  (it "finds an engagement that starts before the range and ends after the range"
    (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id1 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start twelve-days-ago
                                                                     :end one-day-ago})
          {api :api engagement-id2 :result} (create-engagement! api {:employment-id employment-id
                                                                     :project-id project-id
                                                                     :start twelve-days-ago
                                                                     :end six-days-ago})
          found-ids (engagement-ids (find-all-engagements api {:start five-days-ago :end two-days-ago}))]
      (should= [engagement-id1] found-ids)))

  (it "finds an engagement identical to the given range"
     (let [[api employment-id] (test-employment (api-fn))
          {api :api project-id :result} (create-project! api {:name "test"})
          {api :api engagement-id :result} (create-engagement! api {:employment-id employment-id
                                                                    :project-id project-id
                                                                    :start twelve-days-ago
                                                                    :end one-day-ago})
          found-ids (engagement-ids (find-all-engagements api {:start twelve-days-ago :end one-day-ago}))]
       (should= [engagement-id] found-ids)))

  (it "creates an apprenticeship and finds an apprenticeship by id"
    (let [{api :api mentee-id :result} (create-person! (api-fn) {:first-name "John"
                                                                 :last-name "Doe"
                                                                 :email "john.doe@example.com"})
          {api :api mentor-id :result} (create-person! api {:first-name "Jane"
                                                            :last-name "Doe"
                                                            :email "jane.doe@example.com"})
          {api :api apprenticeship-id :result status :status} (create-apprenticeship! api {:person-id mentee-id
                                                                                           :start twelve-days-ago
                                                                                           :end one-day-ago
                                                                                           :skill-level "resident"
                                                                                           :mentorships [{:person-id mentor-id
                                                                                                          :start twelve-days-ago
                                                                                                          :end one-day-ago}]})
          {api :api apprenticeship :result} (find-apprenticeship-by-id api apprenticeship-id)
          mentorship (first (:mentorships apprenticeship))
          apprentice (:person apprenticeship)
          mentor (:person mentorship)]
      (should= :success status)
      (should= apprenticeship-id (:id apprenticeship))
      (should= mentee-id (:person-id apprenticeship))
      (should= twelve-days-ago (:start apprenticeship))
      (should= one-day-ago (:end apprenticeship))
      (should= "resident" (:skill-level apprenticeship))
      (should= mentor-id (:person-id mentorship))
      (should= twelve-days-ago (:start mentorship))
      (should= one-day-ago (:end mentorship))
      (should= "John" (:first-name apprentice))
      (should= "Doe" (:last-name apprentice))
      (should= "john.doe@example.com" (:email apprentice))
      (should= "Jane" (:first-name mentor))
      (should= "Doe" (:last-name mentor))
      (should= "jane.doe@example.com" (:email mentor))
      (should-have-create-timestamps apprenticeship)))

  (it "responds with not found when finding an apprenticeship by id that does not exist"
    (let [api (api-fn)
          response1 (find-apprenticeship-by-id api "bad-id")
          response2 (find-apprenticeship-by-id api "10")]
      (should-respond-with-not-found response1 api)
      (should-respond-with-not-found response2 api)))

  (it "fails to create apprenticeships with no person id"
    (let [{api :api mentor-id :result} (create-person! (api-fn) mentor)
          create-response (create-apprenticeship! api {:start twelve-days-ago
                                                       :end one-day-ago
                                                       :skill-level "resident"
                                                       :mentorships [{:person-id mentor-id
                                                                      :start twelve-days-ago
                                                                      :end one-day-ago}]})]
      (should-fail-with-errors create-response api [response/apprenticeships-missing-person-id])))

  (it "fails to create apprenticeships with invalid person ids"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          create-response1 (create-apprenticeship! api {:person-id "10"
                                                        :start twelve-days-ago
                                                        :end one-day-ago
                                                        :skill-level "resident"
                                                        :mentorships [{:person-id mentor-id
                                                                       :start twelve-days-ago
                                                                       :end one-day-ago}]})
          create-response2 (create-apprenticeship! api {:person-id "ouch!"
                                                        :start twelve-days-ago
                                                        :end one-day-ago
                                                        :skill-level "resident"
                                                        :mentorships [{:person-id mentor-id
                                                                       :start twelve-days-ago
                                                                       :end one-day-ago}]})]
      (should-fail-with-errors create-response1 api [response/apprenticeships-invalid-person-id])
      (should-fail-with-errors create-response2 api [response/apprenticeships-invalid-person-id])))

  (it "fails to create apprenticeships with invalid skill-levels"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response1 (create-apprenticeship! api {:person-id mentee-id
                                                 :start twelve-days-ago
                                                 :end one-day-ago
                                                 :skill-level "invalid"
                                                 :mentorships [{:person-id mentor-id
                                                                :start twelve-days-ago
                                                                :end one-day-ago}]})
          response2 (create-apprenticeship! api {:person-id mentee-id
                                                 :start twelve-days-ago
                                                 :end one-day-ago
                                                 :mentorships [{:person-id mentor-id
                                                                :start twelve-days-ago
                                                                :end one-day-ago}]})]
      (should-fail-with-errors response1 api [response/apprenticeships-invalid-skill-level])
      (should-fail-with-errors response2 api [response/apprenticeships-missing-skill-level])))

  (it "fails to create apprenticeships with missing start dates"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response1 (create-apprenticeship! api {:person-id mentee-id
                                                :end one-day-ago
                                                :skill-level "resident"
                                                :mentorships [{:person-id mentor-id
                                                               :start twelve-days-ago
                                                               :end one-day-ago}]})]
      (should-fail-with-errors response1 api [response/apprenticeships-missing-start-date])))

  (it "fails to create apprenticeships with missing end dates"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response1 (create-apprenticeship! api {:person-id mentee-id
                                                :start twelve-days-ago
                                                :skill-level "resident"
                                                :mentorships [{:person-id mentor-id
                                                               :start twelve-days-ago
                                                               :end one-day-ago}]})]
      (should-fail-with-errors response1 api [response/apprenticeships-missing-end-date])))

  (it "fails to create apprenticeships when the end date is before the start date"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response (create-apprenticeship! api {:person-id mentee-id
                                               :start one-day-ago
                                               :end twelve-days-ago
                                               :skill-level "resident"
                                               :mentorships [{:person-id mentor-id
                                                              :start four-days-ago
                                                              :end one-day-ago}]})]
      (should-fail-with-errors response api [response/apprenticeships-invalid-date-range])))

  (it "fails to create an apprenticeship without any mentorships"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          response1 (create-apprenticeship! api {:person-id mentee-id
                                                 :start twelve-days-ago
                                                 :end one-day-ago
                                                 :skill-level "resident"})
          response2 (create-apprenticeship! api {:person-id mentee-id
                                                 :start twelve-days-ago
                                                 :end one-day-ago
                                                 :skill-level "resident"
                                                 :mentorships []})]
      (should-fail-with-errors response1 api [response/apprenticeships-missing-mentorships])
      (should-fail-with-errors response2 api [response/apprenticeships-missing-mentorships])))

  (it "fails to create an apprenticeship without all mentors present"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response (create-apprenticeship! api {:person-id mentee-id
                                               :start twelve-days-ago
                                               :end one-day-ago
                                               :skill-level "resident"
                                               :mentorships [{:start twelve-days-ago
                                                              :end one-day-ago}
                                                             {:person-id mentor-id
                                                              :start twelve-days-ago
                                                              :end one-day-ago}]})]
      (should-fail-with-errors response api [response/apprenticeships-missing-mentor])))

  (it "fails to create an apprenticeship without all mentorship start dates present"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response (create-apprenticeship! api {:person-id mentee-id
                                               :start twelve-days-ago
                                               :end one-day-ago
                                               :skill-level "resident"
                                               :mentorships [{:person-id mentor-id
                                                              :end one-day-ago}]})]
      (should-fail-with-errors response api [response/apprenticeships-missing-mentorship-start-date])))

  (it "fails to create an apprenticeship without all mentorship end dates present"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response (create-apprenticeship! api {:person-id mentee-id
                                               :start twelve-days-ago
                                               :end one-day-ago
                                               :skill-level "resident"
                                               :mentorships [{:person-id mentor-id
                                                              :start twelve-days-ago}]})]
      (should-fail-with-errors response api [response/apprenticeships-missing-mentorship-end-date])))

  (it "fails to create an apprenticeship with non-chronologically ordered mentorship dates"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response (create-apprenticeship! api {:person-id mentee-id
                                               :start twelve-days-ago
                                               :end one-day-ago
                                               :skill-level "resident"
                                               :mentorships [{:person-id mentor-id
                                                              :start four-days-ago
                                                              :end five-days-ago}]})]
      (should-fail-with-errors response api [response/apprenticeships-invalid-mentorship-date-range])))

  (it "fails to create an apprenticeship with mentorship dates outside the apprenticeships dates"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          response1 (create-apprenticeship! api {:person-id mentee-id
                                                 :start five-days-ago
                                                 :end four-days-ago
                                                 :skill-level "resident"
                                                 :mentorships [{:person-id mentor-id
                                                                :start six-days-ago
                                                                :end four-days-ago}]})
          response2 (create-apprenticeship! api {:person-id mentee-id
                                                 :start five-days-ago
                                                 :end four-days-ago
                                                 :skill-level "resident"
                                                 :mentorships [{:person-id mentor-id
                                                                :start five-days-ago
                                                                :end three-days-ago}]})]
      (should-fail-with-errors response1 api [response/apprenticeships-invalid-mentorship-date-range])
      (should-fail-with-errors response2 api [response/apprenticeships-invalid-mentorship-date-range])))

  (it "returns all apprenticeships"
    (let [{api :api mentee-id :result} (create-person! (api-fn) mentee)
          {api :api mentor-id :result} (create-person! api mentor)
          {api :api apprenticeship-id1 :result } (create-apprenticeship! api {:person-id mentee-id
                                                                              :start twelve-days-ago
                                                                              :end one-day-ago
                                                                              :skill-level "resident"
                                                                              :mentorships [{:person-id mentor-id
                                                                                             :start four-days-ago
                                                                                             :end three-days-ago}]})
          {api :api apprenticeship-id2 :result } (create-apprenticeship! api {:person-id mentee-id
                                                                              :start twelve-days-ago
                                                                              :end one-day-ago
                                                                              :skill-level "craftsman"
                                                                              :mentorships [{:person-id mentor-id
                                                                                             :start twelve-days-ago
                                                                                             :end one-day-ago}]})
          {api :api apprenticeship1 :result} (find-apprenticeship-by-id api apprenticeship-id1)
          {api :api apprenticeship2 :result} (find-apprenticeship-by-id api apprenticeship-id2)
          {api :api apprenticeships :result status :status} (find-all-apprenticeships api)]
      (should= :success status)
      (should= [apprenticeship1 apprenticeship2] apprenticeships)))

  (context "upcoming-apprentice-graduations-by-location"
    (it "returns an empty collection when there are no locations"
      (let [{api :api result :result status :status} (upcoming-apprentice-graduations-by-location (api-fn))]
        (should= :success status)
        (should= #{} result)))

    (it "returns an empty collection of current apprentices when a location has none"
      (let [api (api-fn)
            location-data-one (create-valid-location-data)
            location-data-two (create-valid-location-data)
            {api :api result :result} (create-location! api location-data-one)
            {api :api result :result} (create-location! api location-data-two)
            {api :api result :result status :status} (upcoming-apprentice-graduations-by-location api)]
        (should= #{{:location-name (:name location-data-one)
                    :current-apprentices #{}}
                   {:location-name (:name location-data-two)
                    :current-apprentices #{}}} result)))

    (it "returns all current apprentices for each location with their graduation date"
      (let [mentor-data (create-valid-person-data :first-name "eric" :last-name "smith")
            already-graduated-apprentice-data (create-valid-person-data :first-name "aaron" :last-name "lahey")
            apprentice-one-data (create-valid-person-data :first-name "spencer" :last-name "carvill")
            apprentice-two-data (create-valid-person-data :first-name "brian" :last-name "nystrom")
            apprentice-three-data (create-valid-person-data :first-name "jeff" :last-name "ramnani")
            location-one-data (create-valid-location-data :name "chicago")
            location-two-data (create-valid-location-data :name "london")
            api (api-fn)

            ;; create locations
            {api :api location-one-id :result} (create-location! api location-one-data)
            {api :api location-two-id :result} (create-location! api location-two-data)

            ;; create mentor and apprentices
            {api :api mentor-id :result} (create-person! api mentor-data)
            {api :api already-graduated-apprentice-id :result} (create-person! api already-graduated-apprentice-data)
            {api :api apprentice-one-id :result} (create-person! api apprentice-one-data)
            {api :api apprentice-two-id :result} (create-person! api apprentice-two-data)
            {api :api apprentice-three-id :result} (create-person! api apprentice-three-data)

            ;; create positions
            {api :api position-one-id :result} (create-employment-position! api {:name "resident"})
            {api :api position-two-id :result} (create-employment-position! api {:name "crafter"})

            ;; create employment
            {api :api mentor-employment-id :result} (create-employment! api {:person-id mentor-id
                                                                             :position-id position-two-id
                                                                             :start one-day-ago
                                                                             :end one-day-from-now})
            {api :api already-graduated-apprentice-employment-id :result} (create-employment! api {:person-id already-graduated-apprentice-id
                                                                                                   :position-id position-two-id
                                                                                                   :location-id location-two-id
                                                                                                   :start one-day-ago
                                                                                                   :end one-day-from-now})
            {api :api apprentice-one-employment-id :result} (create-employment! api {:person-id apprentice-one-id
                                                                                     :position-id position-one-id
                                                                                     :location-id location-one-id
                                                                                     :start one-day-ago
                                                                                     :end one-day-from-now})
            {api :api apprentice-two-employment-id :result} (create-employment! api {:person-id apprentice-two-id
                                                                                     :position-id position-one-id
                                                                                     :location-id location-two-id
                                                                                     :start one-day-ago
                                                                                     :end one-day-from-now})
            {api :api apprentice-three-employment-id :result} (create-employment! api {:person-id apprentice-three-id
                                                                                       :position-id position-one-id
                                                                                       :location-id location-one-id
                                                                                       :start one-day-ago
                                                                                       :end one-day-from-now})

            ;; associate people to locations
            {api :api mentor-location-membership-id :result} (create-location-membership! api mentor-employment-id location-one-id (create-valid-location-membership-data))
            {api :api already-graduated-apprentice-location-membership-id :result} (create-location-membership! api already-graduated-apprentice-employment-id location-two-id (create-valid-location-membership-data))
            {api :api location-membership-one-id :result} (create-location-membership! api apprentice-one-employment-id location-one-id (create-valid-location-membership-data))
            {api :api location-membership-two-id :result} (create-location-membership! api apprentice-two-employment-id location-two-id (create-valid-location-membership-data))
            {api :api location-membership-three-id :result} (create-location-membership! api apprentice-three-employment-id location-one-id (create-valid-location-membership-data))

            ;; create the apprenticeships
            {api :api apprenticeship-one-id :result}
            (create-apprenticeship! api {:person-id already-graduated-apprentice-id
                                         :start twelve-days-ago
                                         :end one-day-ago
                                         :skill-level "resident"
                                         :mentorships [{:person-id mentor-id
                                                        :start twelve-days-ago
                                                        :end one-day-ago}]})
            {api :api apprenticeship-one-id :result}
            (create-apprenticeship! api {:person-id apprentice-one-id
                                         :start twelve-days-ago
                                         :end one-day-from-now
                                         :skill-level "resident"
                                         :mentorships [{:person-id mentor-id
                                                        :start twelve-days-ago
                                                        :end one-day-from-now}]})
            {api :api apprenticeship-two-id :result}
            (create-apprenticeship! api {:person-id apprentice-two-id
                                         :start twelve-days-ago
                                         :end two-days-from-now
                                         :skill-level "resident"
                                         :mentorships [{:person-id mentor-id
                                                        :start twelve-days-ago
                                                        :end two-days-from-now}]})
            {api :api apprenticeship-three-id :result}
            (create-apprenticeship! api {:person-id apprentice-three-id
                                         :start twelve-days-ago
                                         :end three-days-from-now
                                         :skill-level "resident"
                                         :mentorships [{:person-id mentor-id
                                                        :start twelve-days-ago
                                                        :end three-days-from-now}]})
            {api :api result :result} (upcoming-apprentice-graduations-by-location api)]
        (should= #{{:location-name "chicago"
                    :current-apprentices #{{:first-name "spencer"
                                            :last-name "carvill"
                                            :graduates-at one-day-from-now}
                                           {:first-name "jeff"
                                            :last-name "ramnani"
                                            :graduates-at three-days-from-now}}}
                   {:location-name "london"
                    :current-apprentices #{{:first-name "brian"
                                            :last-name "nystrom"
                                            :graduates-at two-days-from-now}}}}
                 result)))

    (it "only returns a person for their current location when they change locations mid-apprenticeship"
      (let [mentor-data (create-valid-person-data :first-name "eric" :last-name "smith")
            apprentice-data (create-valid-person-data :first-name "spencer" :last-name "carvill")
            location-one-data (create-valid-location-data :name "chicago")
            location-two-data (create-valid-location-data :name "london")
            api (api-fn)

            ;; create locations
            {api :api location-one-id :result} (create-location! api location-one-data)
            {api :api location-two-id :result} (create-location! api location-two-data)

            ;; create mentor and apprentices
            {api :api mentor-id :result} (create-person! api mentor-data)
            {api :api apprentice-id :result} (create-person! api apprentice-data)

            ;; create position
            {api :api position-id :result} (create-employment-position! api {:name "resident"})

            ;; create employment
            {api :api mentor-employment-id :result} (create-employment! api {:person-id mentor-id
                                                                             :position-id position-id
                                                                             :location-id location-one-id
                                                                             :start one-day-ago
                                                                             :end one-day-from-now})
            {api :api apprentice-employment-id :result} (create-employment! api {:person-id apprentice-id
                                                                                 :position-id position-id
                                                                                 :location-id location-one-id
                                                                                 :start two-days-ago
                                                                                 :end one-day-from-now})

            ;; create the apprenticeship
            {api :api apprenticeship-one-id :result}
            (create-apprenticeship! api {:person-id apprentice-id
                                         :start twelve-days-ago
                                         :end one-day-from-now
                                         :skill-level "resident"
                                         :mentorships [{:person-id mentor-id
                                                        :start twelve-days-ago
                                                        :end one-day-from-now}]})

            ;; associate people to locations
            {mentor-location-membership-id :result} (find-all-location-memberships-for-employment api mentor-employment-id)
            {apprentice-location-membership-id :result} (find-all-location-memberships-for-employment api apprentice-employment-id)

            ;; apprentice changes location mid-apprenticeship
            {api :api location-membership-one-id :result}
            (create-location-membership! api apprentice-employment-id location-two-id (create-valid-location-membership-data :start one-day-ago))

            {api :api result :result} (upcoming-apprentice-graduations-by-location api)]
        (should= #{{:location-name "london"
                    :current-apprentices #{{:first-name "spencer"
                                            :last-name "carvill"
                                            :graduates-at one-day-from-now}}}
                   {:location-name "chicago"
                    :current-apprentices #{}}}
                 result))))

  (context "/create-director-engagement!"
    (it "does not create a director-engagement without an person-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {:person nil})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-missing-person-id])))

    (it "does not create a director-engagement without a valid person-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {:person-id 1337})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-invalid-person-id])))

    (it "does not create a director-engagement without a project-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {:project nil})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-missing-project-id])))

    (it "does not create a director-engagement without a valid project-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {:project-id 12345})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-invalid-project-id])))

    (it "requires a start-date"
      (let [[api director-engagement-data] (create-valid-director-engagement-data
                                  (api-fn)
                                  {:start nil})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-missing-start-date])))

    (it "allows end to be the same date as start"
      (let [[api director-engagement-data] (create-valid-director-engagement-data
                                  (api-fn)
                                  {:start five-days-ago :end five-days-ago})
            response (create-director-engagement! api director-engagement-data)]
        (should= :success (:status response))))

    (it "does not create a director-engagement if end is before start"
      (let [[api director-engagement-data] (create-valid-director-engagement-data
                                  (api-fn)
                                  {:start one-day-ago :end five-days-ago})
            response (create-director-engagement! api director-engagement-data)]
        (should-fail-with-errors response api [response/director-engagements-invalid-date-range])))

    (it "allows nil end date if start date is present"
      (let [[api director-engagement-data] (create-valid-director-engagement-data
                                  (api-fn)
                                  {:start one-day-ago :end nil})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            {api :api director-engagement :result} (find-director-engagement-by-id api director-engagement-id)]
        (should= one-day-ago (:start director-engagement))
        (should= nil (:end director-engagement)))))

  (context "/update-director-engagement!"
    (it "does not update a director-engagement without an person-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:person-id nil})]
        (should-fail-with-errors response api [response/director-engagements-missing-person-id])))

    (it "does not update a director-engagement without a valid person-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:person-id 1337})]
        (should-fail-with-errors response api [response/director-engagements-invalid-person-id])))

    (it "does not update a director-engagement without a project-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:project-id nil})]
        (should-fail-with-errors response api [response/director-engagements-missing-project-id])))

    (it "does not update a director-engagement without a valid project-id"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:project-id 1337})]
        (should-fail-with-errors response api [response/director-engagements-invalid-project-id])))

    (it "requires a start-date"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:start nil})]
        (should-fail-with-errors response api [response/director-engagements-missing-start-date])))

    (it "does not update a director-engagement if end is before start"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            response (update-director-engagement! api director-engagement-id {:start one-day-ago :end five-days-ago})]
        (should-fail-with-errors response api [response/director-engagements-invalid-date-range])))

    (it "allows nil end date if start date is present"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api director-engagement-id :result} (create-director-engagement! api director-engagement-data)
            {api :api} (update-director-engagement! api director-engagement-id {:start one-day-ago :end nil})
            {api :api director-engagement :result} (find-director-engagement-by-id api director-engagement-id)]
        (should= one-day-ago (:start director-engagement))
        (should= nil (:end director-engagement)))))

  (it "returns a not-found response if there is no director-engagement with the given id"
    (let [{api :api status :status} (find-director-engagement-by-id (api-fn) 33)]
      (should= :not-found status)))

  (context "/find-current-directors"
    (it "finds all current directors"
      (let [[api old-director-engagement-data] (create-valid-director-engagement-data
                                                 (api-fn)
                                                 {:person {:first-name "Kevin"
                                                           :last-name "LiddlestOfThemAll"}
                                                  :project {:name "stockroom"}
                                                  :end one-day-ago})
            [api current-director-engagement-data] (create-valid-director-engagement-data api {})
            {api :api director-engagement-id-1 :result} (create-director-engagement! api old-director-engagement-data)
            {api :api director-engagement-id-2 :result} (create-director-engagement! api current-director-engagement-data)
            {api :api directors :result} (find-current-directors api)
            current-director (first directors)]
        (should= 1 (count directors))
        (should-not-be-nil (:id current-director))
        (should= (:person-id current-director-engagement-data) (:id current-director))
        (should= "Sandro" (:first-name current-director))
        (should= "PadinMyStats" (:last-name current-director))))

    (it "does not return the same person multiple times"
      (let [[api current-director-engagement-data] (create-valid-director-engagement-data (api-fn) {})
            {api :api current-director-engagement-id :result} (create-director-engagement! api current-director-engagement-data)
            person-id (:person-id current-director-engagement-data)
            [api old-director-engagement-data] (create-valid-director-engagement-data
                                                 api
                                                 {:person-id person-id
                                                  :project {:name "stockroom"}})
            {api :api old-director-engagement-id :result} (create-director-engagement! api old-director-engagement-data)
            {api :api directors :result} (find-current-directors api)
            current-director (first directors)
            get-person-id-fn (fn [director-engagement-id]
                               (get-in (find-director-engagement-by-id api director-engagement-id) [:result :person-id]))
            current-director-engagement-person-id (get-person-id-fn current-director-engagement-id)
            old-director-engagement-person-id (get-person-id-fn old-director-engagement-id)]
        (should= person-id current-director-engagement-person-id)
        (should= person-id old-director-engagement-person-id)
        (should= 1 (count directors))
        (should-not-be-nil (:id current-director))
        (should= person-id (:id current-director))))

    (it "finds people whose director-engagements don't have an end date"
      (let [[api director-engagement-data] (create-valid-director-engagement-data (api-fn) {:end nil})
            {api :api id :result} (create-director-engagement! api director-engagement-data)
            {api :api directors :result} (find-current-directors api)]
        (should= 1 (count directors)))))

  (context "/find-all-director-engagements-by-person-id"
    (it "finds all director engagements for a director"
      (let [[api director-engagement-data-2] (create-valid-director-engagement-data
                                               (api-fn)
                                               {:person {:first-name "Aaron"
                                                         :last-name "Laheywhoooo"}
                                                :project {:name "stockroom"}
                                                :start one-day-ago
                                                :end one-day-from-now})
            director-person-id (:person-id director-engagement-data-2)
            [api director-engagement-data-1] (create-valid-director-engagement-data
                                             api
                                              {:project {:name "Vision"}
                                               :person-id director-person-id
                                               :start ten-days-ago
                                               :end five-days-ago})
            {api :api director-engagement-id-1 :result} (create-director-engagement! api director-engagement-data-2)
            {api :api director-engagement-id-2 :result} (create-director-engagement! api director-engagement-data-1)
            {api :api director-engagements :result} (find-all-director-engagements-by-person-id api director-person-id)
            director-engagement-1 (first director-engagements)
            director-engagement-2 (second director-engagements)]
        (should= 2 (count director-engagements))
        (should= "Vision" (:name (:project director-engagement-1)))
        (should= ten-days-ago (:start director-engagement-1))
        (should= five-days-ago (:end director-engagement-1))
        (should= (:project-id director-engagement-data-1) (:project-id director-engagement-1))
        (should= (:project-id director-engagement-data-1) (:id (:project director-engagement-1)))
        (should= "stockroom" (:name (:project director-engagement-2)))
        (should= one-day-ago (:start director-engagement-2))
        (should= one-day-from-now (:end director-engagement-2))
        (should= (:project-id director-engagement-data-2) (:project-id director-engagement-2))
        (should= (:project-id director-engagement-data-2) (:id (:project director-engagement-2))))))

  (context "create-location!"
    (it "saves a new location to the db"
      (let [location-data (create-valid-location-data)
            api (api-fn)
            {api :api location-id :result status :status} (create-location! api location-data)
            {api :api created-location :result} (find-location-by-id api location-id)]
        (should= :success status)
        (should= (:name location-data) (:name created-location))
        (should-have-create-timestamps created-location)))

    (it "returns an error when the location does not have a name"
      (let [invalid-location-data (create-valid-location-data :name nil)
            api (api-fn)
            {api :api errors :errors status :status} (create-location! api invalid-location-data)]
        (should= :failure status)
        (should= [response/locations-missing-name] errors)))

    (it "returns an error when the location's name is blank"
      (let [invalid-location-data (create-valid-location-data :name "")
            api (api-fn)
            {api :api errors :errors status :status} (create-location! api invalid-location-data)]
        (should= :failure status)
        (should= [response/locations-missing-name] errors)))

    (it "returns an error when the location's name is not unique"
      (let [location-data (create-valid-location-data)
            api (api-fn)
            {api :api initial-create-status :status} (create-location! api location-data)
            {api :api errors :errors status :status} (create-location! api location-data)]
        (should= :success initial-create-status)
        (should= :failure status)
        (should= [response/locations-duplicate-name] errors))))

  (context "find-all-locations"
    (it "returns all locations"
      (let [{api :api location1-id :result} (create-location! (api-fn) {:name "Chicago"})
            {api :api location2-id :result} (create-location! api {:name "London"})
            find-response (find-all-locations api)]

        (should= :success (:status find-response))
        (should== [location1-id location2-id] (map :id (:result find-response))))))

  (context "find-location-by-id"
    (it "returns a successful response when the location is found"
      (let [location-data (create-valid-location-data)
            api (api-fn)
            {api :api location-id :result} (create-location! api location-data)
            {api :api location :result status :status} (find-location-by-id api location-id)]
        (should= :success status)
        (should= (:name location-data) (:name location))))

    (it "returns a not found error when the location is not found"
      (let [api (api-fn)
            response (find-location-by-id api 42)]
        (should-respond-with-not-found response api))))

  (context "create-location-membership!"
    (it "saves a new location-membership to the db"
      (let [person-data (create-valid-person-data)
            location-data (create-valid-location-data)
            location-membership-data (create-valid-location-membership-data)
            api (api-fn)
            {api :api person-id :result} (create-person! api person-data)
            {api :api location-id :result} (create-location! api location-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :location-id location-id
                                                                      :start three-days-ago})
            {api :api location-membership-id :result} (create-location-membership! api employment-id location-id location-membership-data)
            {api :api created-location-membership :result status :status} (find-location-membership-by-id api location-membership-id)]
        (should= :success status)
        (should= (:start location-membership-data) (:start created-location-membership))
        (should= employment-id (:employment-id created-location-membership))
        (should= location-id (:location-id created-location-membership))
        (should-have-create-timestamps created-location-membership))

      (it "returns an error when the employment id is nil"
        (let [employment-id nil
              location-data (create-valid-location-data)
              location-membership-data (create-valid-location-membership-data)
              api (api-fn)
              {api :api location-id :result} (create-location! api location-data)
              {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
              {api :api errors :errors status :status} (create-location-membership! api employment-id location-id location-membership-data)]
          (should= :failure status)
          (should= [response/location-memberships-missing-employment-id] errors)))

      (it "returns an error when the location id is nil"
        (let [person-data (create-valid-person-data)
              location-id nil
              location-membership-data (create-valid-location-membership-data)
              api (api-fn)
              {api :api person-id :result} (create-person! api person-data)
              {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
              {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                        :position-id position-id
                                                                        :start three-days-ago})
              {api :api errors :errors status :status} (create-location-membership! api employment-id location-id location-membership-data)]
          (should= :failure status)
          (should= [response/location-memberships-missing-location-id] errors)))))

  (context "find-location-membership-by-id"
    (it "returns a successful response when the location-membership is found"
      (let [person-data (create-valid-person-data)
            location-data (create-valid-location-data)
            location-membership-data (create-valid-location-membership-data)
            api (api-fn)
            {api :api person-id :result} (create-person! api person-data)
            {api :api location-id :result} (create-location! api location-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :location-id location-id
                                                                      :position-id position-id
                                                                      :start three-days-ago})
            {api :api location-membership-id :result} (create-location-membership! api employment-id location-id location-membership-data)
            {api :api created-location-membership :result status :status} (find-location-membership-by-id api location-membership-id)]
        (should= :success status)))

    (it "returns a not found error when the location-membership is not found"
      (let [api (api-fn)
            response (find-location-membership-by-id api 42)]
        (should-respond-with-not-found response api))))

  (describe "Finding current people by position"

    (it "finds none"
      (let [api (api-fn)]
        (should= 0 (count (:result (find-current-people-by-position api "craftsman"))))))

    (it "finds the only matching person"
      (let [api (api-fn)
            person-data (create-valid-person-data)
            {api :api person-id :result} (create-person! api person-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api location-id :result} (create-location! api {:name "chicago"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :location-id location-id
                                                                      :start one-day-ago
                                                                      :end one-day-from-now})
            matching-people (:result (find-current-people-by-position api "craftsman"))]
        (should= 1 (count matching-people))
        (should= (:first-name person-data) (:first-name (first matching-people)))
        (should= (:last-name person-data) (:last-name (first matching-people)))
        (should= (:email person-data) (:email (first matching-people)))
        (should= [person-id] (map #(:id %) matching-people))))

    (it "does not return a person for a non-matching position"
      (let [api (api-fn)
            {api :api person-id :result} (create-person! api (create-valid-person-data))
            {api :api position-id :result} (create-employment-position! api {:name "resident"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :start one-day-ago
                                                                      :end one-day-from-now})]
        (should= 0 (count (:result (find-current-people-by-position api "craftsman"))))))

    (it "only returns the matching person"
      (let [api (api-fn)
            {api :api person-one-id :result} (create-person! api (create-valid-person-data))
            {api :api person-two-id :result} (create-person! api (create-valid-person-data))
            {api :api position-one-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api position-two-id :result} (create-employment-position! api {:name "resident"})
            {api :api location-id :result} (create-location! api {:name "chicago"})
            {api :api employment-one-id :result} (create-employment! api {:person-id person-one-id
                                                                          :position-id position-one-id
                                                                          :location-id location-id
                                                                          :start one-day-ago
                                                                          :end one-day-from-now})
            {api :api employment-two-id :result} (create-employment! api {:person-id person-two-id
                                                                          :position-id position-two-id
                                                                          :location-id location-id
                                                                          :start one-day-ago
                                                                          :end one-day-from-now})]
        (should= 1 (count (:result (find-current-people-by-position api "craftsman"))))))

  (it "returns multiple people for one position"
    (let [api (api-fn)
          person-data (create-valid-person-data)
          {api :api person-one-id :result} (create-person! api (create-valid-person-data))
          {api :api person-two-id :result} (create-person! api (create-valid-person-data))
          {api :api position-id :result} (create-employment-position! api {:name "resident"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api employment-id :result} (create-employment! api {:person-id person-one-id
                                                                    :position-id position-id
                                                                    :location-id location-id
                                                                    :start one-day-ago
                                                                    :end one-day-from-now})
          {api :api employment-id :result} (create-employment! api {:person-id person-two-id
                                                                    :position-id position-id
                                                                    :location-id location-id
                                                                    :start one-day-ago
                                                                    :end one-day-from-now})
          matching-people (:result (find-current-people-by-position api "resident"))]
      (should= 2 (count matching-people))))

  (it "does not return a person whose employment has ended"
    (let [api (api-fn)
          {api :api person-id :result} (create-person! api (create-valid-person-data))
          {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                    :position-id position-id
                                                                    :location-id location-id
                                                                    :start three-days-ago
                                                                    :end one-day-ago})]
      (should= 0 (count (:result (find-current-people-by-position api "craftsman"))))))

  (it "returns a person when querying with a different position"
    (let [api (api-fn)
          person-data (create-valid-person-data)
          {api :api person-id :result} (create-person! api person-data)
          {api :api position-id :result} (create-employment-position! api {:name "resident"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                    :position-id position-id
                                                                    :location-id location-id
                                                                    :start one-day-ago
                                                                    :end one-day-from-now})]
      (should= 1 (count (:result (find-current-people-by-position api "resident"))))))

  (it "returns a person with no end date"
    (let [api (api-fn)
          {api :api person-id :result} (create-person! api (create-valid-person-data))
          {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                    :position-id position-id
                                                                    :location-id location-id
                                                                    :start three-days-ago})]
      (should= 1 (count (:result (find-current-people-by-position api "craftsman"))))))

  (it "does not return a duplicate person if they have the same employment twice"
    (let [api (api-fn)
          {api :api person-id :result} (create-person! api (create-valid-person-data))
          {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
          {api :api location-id :result} (create-location! api {:name "chicago"})
          {api :api} (create-employment! api {:person-id person-id
                                              :position-id position-id
                                              :location-id location-id
                                              :start three-days-ago})
          {api :api} (create-employment! api {:person-id person-id
                                              :position-id position-id
                                              :location-id location-id
                                              :start three-days-ago})]
      (should= 1 (count (:result (find-current-people-by-position api "craftsman")))))))

  (describe "Finding current location memberships"
    (it "has none for no people"
      (let [api (api-fn)]
        (should= {} (:result (find-current-location-membership-for-people api [])))))

    (it "has one for a person with one location membership"
      (let [api (api-fn)
            {api :api person-id :result} (create-person! api (create-valid-person-data))
            location-data (create-valid-location-data)
            {api :api location-id :result foo :status} (create-location! api location-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :location-id location-id
                                                                      :start three-days-ago})
            {api :api location-membership-id :result} (create-location-membership! api employment-id location-id (create-valid-location-membership-data))
            current-locations (:result (find-current-location-membership-for-people api [person-id]))]
        (should= (:name location-data) (:name (current-locations person-id)))
        (should= location-id (:id (current-locations person-id)))))

    (it "does not return the location for a person who was not queried"
      (let [api (api-fn)
            {api :api person-id :result} (create-person! api (create-valid-person-data))
            location-data (create-valid-location-data)
            {api :api location-id :result} (create-location! api location-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :start three-days-ago})
            {api :api location-membership-id :result} (create-location-membership! api employment-id location-id (create-valid-location-membership-data))
            current-locations (:result (find-current-location-membership-for-people api []))]
        (should= {} current-locations)))

    (it "returns a current location for each of two people"
      (let [api (api-fn)
            {api :api person-one-id :result} (create-person! api (create-valid-person-data))
            {api :api person-two-id :result} (create-person! api (create-valid-person-data))
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            location-data (create-valid-location-data)
            {api :api location-id :result} (create-location! api location-data)
            {api :api employment-one-id :result} (create-employment! api {:person-id person-one-id
                                                                          :position-id position-id
                                                                          :location-id location-id
                                                                          :start three-days-ago})
            {api :api employment-two-id :result} (create-employment! api {:person-id person-two-id
                                                                          :position-id position-id
                                                                          :location-id location-id
                                                                          :start three-days-ago})
            {api :api} (create-location-membership! api employment-one-id location-id (create-valid-location-membership-data))
            {api :api} (create-location-membership! api employment-two-id location-id (create-valid-location-membership-data))
            current-locations (:result (find-current-location-membership-for-people api [person-one-id person-two-id]))]
        (should= 2 (count current-locations))))

    (it "deletes a location membership"
    (let [[api location-membership-id] (test-location-membership (api-fn))
          {api :api :as delete-response} (delete-location-membership! api location-membership-id)
          find-response (find-location-membership-by-id api location-membership-id)]
      (should= :success (:status delete-response))
      (should-respond-with-not-found find-response api)))


    (it "returns nothing for someone who has no location set"
      (let [api (api-fn)
            {api :api person-id :result} (create-person! api (create-valid-person-data))
            current-locations (:result (find-current-location-membership-for-people api [person-id]))]
        (should= {} current-locations)))

    (it "returns the most recent location for a person with multiple"
      (let [api (api-fn)
            {api :api person-id :result} (create-person! api (create-valid-person-data))
            first-location-data {:name "First"}
            second-location-data {:name "Second"}
            third-location-data {:name "Third"}
            {api :api first-location-id :result} (create-location! api first-location-data)
            {api :api second-location-id :result} (create-location! api second-location-data)
            {api :api third-location-id :result} (create-location! api third-location-data)
            {api :api position-id :result} (create-employment-position! api {:name "craftsman"})
            {api :api employment-id :result} (create-employment! api {:person-id person-id
                                                                      :position-id position-id
                                                                      :location-id first-location-id
                                                                      :start twelve-days-ago})
            {api :api} (create-location-membership! api employment-id third-location-id {:start three-days-ago})
            {api :api} (create-location-membership! api employment-id second-location-id {:start ten-days-ago})
            current-locations (:result (find-current-location-membership-for-people api [person-id]))]
        (should= "Third" (:name (current-locations person-id)))))))
