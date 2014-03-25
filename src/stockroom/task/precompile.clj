(ns stockroom.task.precompile
  (:require [conveyor.core :refer [with-pipeline-config]]
            [conveyor.precompile :refer [precompile]]
            [stockroom.config :as config]))

(defn precompile-config []
  (:precompile (config/read-config "conveyor.clj")))

(def assets ["admin.css" "html5shiv.js"
             #".*\.pdf" #".*\.eot" #".*\.svg" #".*\.ttf" #".*\.woff" #".*\.jpg" #".*\.png" #".*\.ico"])

(defn -main [& args]
  (let [exit-code (with-pipeline-config
                    (precompile-config)
                    (precompile assets)
                    0)]
    (println "Done precompiling.")
    exit-code))
