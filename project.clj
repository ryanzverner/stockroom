(defproject com.ryan/stockroom "1.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]

                 [chee "2.0.0"]
                 [korma "0.3.2"]
                 [mysql/mysql-connector-java "5.1.31"]
                 [org.clojure/tools.logging "0.3.0"]

                 ; to configure C3P0 logging
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]

                 ; common web dependencies
                 [compojure "1.1.6"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-jetty-adapter "1.2.2"]

                 ; admin dependencies
                 [clojurewerkz/route-one "1.1.0"]
                 [com.8thlight/hiccup "1.1.2"]
                 [conveyor "0.2.8"]
                 [metis "0.3.3"]
                 [stuarth/clj-oauth2 "0.3.2"]

                 ; api dependencies
                 [camel-snake-kebab "0.1.4"]
                 [clj-jwt "0.0.4"]
                 [com.fasterxml.jackson.core/jackson-core "2.2.1"]
                 [com.google.api-client/google-api-client "1.18.0-rc"]
                 [com.google.http-client/google-http-client-jackson2 "1.18.0-rc" :exclusions [org.apache.httpcomponents/httpclient]]
                 [clj-http "1.0.0"]
                 [org.clojure/core.cache "0.6.4"]
                 [ring-middleware-format "0.3.2"]

                 ; this resolves the conflict between clj-jwt and clj-oauth2
                 [org.clojure/data.json "0.2.3"]

                 ; this resolves the conflict between clj-jwt and ring-core
                 [clj-time "0.6.0"]

                 ; this resolves dependencies with clj-http
                 [org.clojure/tools.reader "0.8.5"]
                 [commons-logging "1.1.3"]
                 [commons-codec "1.9"]
                 [com.fasterxml.jackson.core/jackson-core "2.3.2"]
                 ]

  :pedantic? :abort

  :profiles {:dev {:dependencies [[speclj "3.0.2"]

                                  ; for wrap-reload
                                  [ring/ring-devel "1.2.2"]

                                  ; assets
                                  [conveyor-compass "0.2.8"]
                                  [conveyor-sass "0.2.8"]
                                  ]

                   :plugins [[speclj "3.0.2"]]

                   :main stockroom.development-main

                   :test-paths ["spec"]}

             :prod {:aot :all
                    :main stockroom.production-main}
             }

  )
