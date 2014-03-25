(ns stockroom.api.util.response
  (:require [ring.util.response :as response]
            [stockroom.v1.ring :as wring]))

(def missing-id-token-error
  {:code :missing-id-token
   :description "Must provide an open id token to access the api."})

(def invalid-id-token-error
  {:code :invalid-id-token
   :description "The given id token is invalid."})

(def expired-id-token-error
  {:code :expired-id-token
   :description "The given id token has expired."})

(def user-not-found-error
  {:code :user-not-found
   :description "We received your token, but there is no user with the given UID and provider."})

(def unauthorized-error
  {:code :unauthorized
   :description "You are not authorized to access this resource."})

(def director-engagements-malformatted-start-date
  {:code :director-engagements/malformatted-start-date
   :description "The \"start\" value must be formated YYYY-MM-DD."})

(def director-engagements-malformatted-end-date
  {:code :director-engagements/malformatted-end-date
   :description "The \"end\" value must be formated YYYY-MM-DD."})

(def engagements-malformatted-start-date
  {:code :engagements/malformatted-start-date
   :description "The \"start\" value must be formated YYYY-MM-DD."})

(def engagements-malformatted-end-date
  {:code :engagements/malformatted-end-date
   :description "The \"end\" value must be formated YYYY-MM-DD."})

(def engagements-start-end-xnor-required
  {:code :engagements/start-end-xnor-required
   :description "The \"start\" and \"end\" values are both required if either is present."})

(def employments-malformatted-start-date
  {:code :employments/malformatted-end-date
   :description "The \"start\" value must be formated YYYY-MM-DD."})

(def employments-malformatted-end-date
  {:code :employments/malformatted-end-date
   :description "The \"end\" value must be formated YYYY-MM-DD."})

(def people-missing-first-name
  {:code :people/missing-first-name
   :description "A first name is required to create a person."})

(def people-missing-last-name
  {:code :people/missing-last-name
   :description "A last name is required to create a person."})

(def apprenticeships-malformatted-start-date
  {:code :apprenticeships/malformatted-end-date
   :description "The \"start\" value must be formated YYYY-MM-DD."})

(def apprenticeships-malformatted-end-date
  {:code :apprenticeships/malformatted-end-date
   :description "The \"end\" value must be formated YYYY-MM-DD."})

(def location-memberships-malformatted-start-date
  {:code :location-memberships/malformatted-start-date
   :description "The \"start\" value must be formated YYYY-MM-DD."})

(def person-missing-search-parameters
  {:code :people/missing-search-parameters
   :description "Must include first name, last name or email when searching for people."})

(defn bad-request-response [api errors]
  (-> {:errors errors}
    response/response
    (response/status 400)
    (wring/set-user-api api)))

(defn not-found-response [api]
  (-> (response/response "")
    (response/status 404)
    (wring/set-user-api api)))

(defn failure-response [api errors]
  (-> {:errors errors}
    response/response
    (response/status 422)
    (wring/set-user-api api)))

(defn unauthorized-response [api]
  (-> {:errors [unauthorized-error]}
    response/response
    (response/status 403)
    (wring/set-user-api api)))

(defn handle-response [response]
  (case (:status response)
    :not-found (not-found-response (:api response))
    :unauthorized (unauthorized-response (:api response))
    (throw (Exception. (format "Got unknown status: %s" (:status response))))))

(defmacro when-status [& args]
  (let [response (last args)
        conditions (butlast args)
        condition-map (loop [[cond fn & more] conditions acc {}]
                        (if cond
                          (recur more (assoc acc cond fn))
                          acc))]
    `(let [resp# ~response]
       (case (:status resp#)
       :success (if-let [f# ~(:success condition-map)]
                  (f# (:api resp#) (:result resp#))
                  (throw (Exception. "Got success status but could handle it.")))
       :failure (if-let [f# ~(:failure condition-map)]
                  (f# (:api resp#) (:errors resp#))
                  (failure-response (:api resp#) (:errors resp#)))
       :not-found (if-let [f# ~(:not-found condition-map)]
                    (f# (:api resp#))
                    (handle-response resp#))
       (handle-response resp#)))))
