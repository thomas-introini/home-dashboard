(ns home-dashboard.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-file "sensor.db")

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname db-file})

(defn init-db []
  (when-not (.exists (java.io.File. db-file))
    (jdbc/execute! db ["CREATE TABLE IF NOT EXISTS sensor (id INTEGER PRIMARY KEY AUTOINCREMENT, temperature INTEGER, humidity INTEGER, date DATETIME DEFAULT CURRENT_TIMESTAMP)"])))

(init-db)

(defn fetch-sensor-data [limit offset]
  (jdbc/query db ["SELECT * FROM sensor ORDER BY date DESC LIMIT ? OFFSET ?" limit offset]))

(defn fetch-grouped-sensor-data [from to interval]
  (jdbc/query
   db
   ["SELECT
       datetime(strftime('%s', date) / ? * ?, 'unixepoch') as date,
       AVG(temperature) as temperature,
       AVG(humidity) as humidity
     FROM sensor
     WHERE date BETWEEN ? AND ?
     GROUP BY 1
     ORDER BY 1" interval interval (.toString from) (.toString to)]))

(defn insert-sensor-data [temperature humidity timestamp]
  (jdbc/insert! db :sensor {:temperature temperature :humidity humidity :date timestamp}))
