(ns home-dashboard.utils
  (:require [clojure.java.io :as io]))

(defn slurp-resource [resource-path]
  (some-> (io/resource resource-path) slurp))

(def ts-formatter
  (.withZone
   (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")
   (java.time.ZoneId/of "UTC")))

(def hr-formatter
  (.withZone
   (java.time.format.DateTimeFormatter/ofPattern "EEEE dd/MM/yyyy HH:mm:ss")
   (java.time.ZoneId/of "Europe/Rome")))

(def hr-short-formatter
  (.withZone
   (java.time.format.DateTimeFormatter/ofPattern "dd/MM HH:mm")
   (java.time.ZoneId/of "Europe/Rome")))

(defn format-sql-ts-short [ts]
  (.format hr-short-formatter (.parse ts-formatter ts)))

(defn format-sql-ts [ts]
  (->> ts
       (.parse ts-formatter)
       (.format hr-formatter)))

(defn real->str [n]
  (if (integer? n) n (format  "%.1f" n)))
