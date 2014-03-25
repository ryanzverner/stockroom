(ns stockroom.admin.util.response
  (:require [clojurewerkz.urly.core :as urly]
            [hiccup.def :refer [defhtml]]
            [ring.util.response :as response]
            [stockroom.admin.url-helper :as urls]
            [stockroom.v1.ring :as wresponse]))

(defn reset-session [response]
  (assoc response :session nil))

(defn request-url [request]
  (str (urly/mutate-query (:uri request) (:query-string request))))

(defn redirect-to-login-url [context request]
  (-> context
    (urls/login-url {:return-url (request-url request)})
    response/redirect))

(defhtml render-not-found []
  [:div
   [:h1 "Sorry..."]
   [:p "The page you are looking for cannot be found."]])

(defn not-found-response [api]
  (-> (render-not-found)
    response/not-found
    (wresponse/set-user-api api)))

(defhtml render-unauthorized []
  [:div
   [:h1 "Sorry..."]
   [:p "I can't let you see this page."]])

(defn unauthorized-response [api]
  (-> (render-unauthorized)
    response/response
    (response/status 401)
    (wresponse/set-user-api api)))

(defn api-errors-to-web-errors [errors errors-mapping]
  (reduce
    (fn [acc {:keys [code description]}]
      (if-let [{:keys [key message]} (code errors-mapping)]
        (update-in acc [key] #(conj % message))
        (update-in acc [:base] #(conj % description))))
    {}
    errors))

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
                  (throw (Exception. "Got failure status but could not handle it.")))
       :not-found (if-let [f# ~(:not-found condition-map)]
                    (f# (:api resp#))
                    (handle-response resp#))
       (handle-response resp#)))))
