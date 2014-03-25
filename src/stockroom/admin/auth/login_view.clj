(ns stockroom.admin.auth.login-view
  (:require [hiccup.def :refer [defhtml]]
            [ring.util.codec :refer [form-encode]]
            [stockroom.api.open-id-token :as open-id-token]))

(defhtml render-login-view [view-data]
  [:a {:href (:login-url view-data)} "Please sign in with Google"])

(defhtml render-user-not-found [provider uid]
  [:div
   [:h1 "Sorry..."]
   [:p "Looks like your Google Account is not listed in our system."]
   [:p "To be added, contact "
    [:a {:href "mailto:hello@abc.com" :target "_blank"} "hello@abc.com"]
    " with the information below:"]
   [:ul
    [:li "Provider: " provider]
    [:li "UID: " uid]]])

(defhtml render-login-error []
  [:div
   [:h1 "Sorry..."]
   [:p "There was an error processing the authentication response from Google."]
   [:p "Contact "
    [:a {:href "mailto:hello@abc.com" :target "_blank"} "hello@abc.com"]
    " if the problem persists."]])
