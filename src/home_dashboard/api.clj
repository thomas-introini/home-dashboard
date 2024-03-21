(ns home-dashboard.api
  (:require
   [home-dashboard.db :refer [insert-sensor-data]]
   [home-dashboard.utils :refer [ts-formatter]]))

(defn api-insert-sensor-data [r]
  (let [temperature (get-in r [:params :temperature])
        humidity (get-in r [:params :humidity])
        now (.format ts-formatter (java.time.Instant/now))
        err (cond (nil? temperature) "temperature is required"
                  (nil? humidity) "humidity is required"
                  :else nil)]
    (if err
      {:status 400 :headers {"Content-Type" "application/json"} :body (format "{\"message\": \"%s\"}" err)}
      (do (insert-sensor-data temperature humidity now)
          {:status 201
           :headers {"Content-Type" "application/json"}
           :body "{\"message\": \"Data stored successfully\"}"}))))
