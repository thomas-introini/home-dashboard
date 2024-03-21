(defproject home-dashboard "1.0.0"
  :description "A dashboard for displaying data from a DHT22 sensor attached to a Raspberry Pi"
  :url "https://home.thomasintroini.it"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [hiccup "2.0.0-RC3"]
                 [clojure.java-time "1.4.2"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.xerial/sqlite-jdbc "3.45.1.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler home-dashboard.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
