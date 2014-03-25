(ns stockroom.admin.util.request)

(defn set-current-user [request user]
  (assoc request :admin/current-user user))

(defn current-user [request]
  (:admin/current-user request))

(defn current-user-id [request]
  (-> request
    current-user
    :id))

(defn set-uid-and-provider [response data]
  (-> response
    (assoc-in [:session :admin/uid] (:uid data))
    (assoc-in [:session :admin/provider] (name (:provider data)))))

(defn current-uid-and-provider [request]
  (let [session (:session request)]
    {:uid      (:admin/uid session)
     :provider (:admin/provider session)}))
