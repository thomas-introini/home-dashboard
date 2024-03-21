(ns home-dashboard.handler-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [home-dashboard.handler :refer [app]]
   [ring.mock.request :as mock]
   [clojure.string :as str]))

(deftest test-app
  (testing "home page"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (str/includes? (:body response) "Sensor dashboard"))))
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
