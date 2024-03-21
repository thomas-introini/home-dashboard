(ns home-dashboard.handler
  (:require
   [compojure.core :refer [defroutes GET POST routes]]
   [compojure.route :as route]
   [home-dashboard.api :refer [api-insert-sensor-data]]
   [home-dashboard.home-page :refer [api-get-more-rows api-get-sensor-chart
                                     home-page]]
   [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]))

(defroutes app-routes
  (GET "/" [] home-page)
  (GET "/api/get-more-rows" [] api-get-more-rows)
  (GET "/api/get-sensor-chart" [] api-get-sensor-chart)
  (route/not-found "<h1>Not Found</h1>"))

(defroutes api-routes
  (POST "/api/data" [] api-insert-sensor-data))

(def app
  (routes
   (wrap-defaults api-routes api-defaults)
   (wrap-defaults app-routes site-defaults)))
