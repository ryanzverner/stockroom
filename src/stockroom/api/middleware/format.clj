(ns stockroom.api.middleware.format
  (:require [camel-snake-kebab :refer [->camelCaseString
                                       ->kebab-case-keyword]]
            [cheshire.core :as json]
            [ring.middleware.format-params :refer [wrap-json-params]]
            [ring.middleware.format-response :refer [make-encoder
                                                     wrap-restful-response]]
            [stockroom.middleware.accept :refer [wrap-accept]]))

(defn to-json [data]
  (json/generate-string data {:key-fn ->camelCaseString}))

(defn from-json [s]
  (json/parse-string s ->kebab-case-keyword))

(defn wrap-format [handler fallback-handler]
  (-> handler
    (wrap-json-params :decoder from-json)
    (wrap-restful-response :formats [(make-encoder to-json "application/json")])
    (wrap-accept fallback-handler "application" "json")))
