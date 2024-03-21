(ns home-dashboard.home-page
  (:require
   [clojure.string :as str]
   [hiccup2.core :as h]
   [home-dashboard.db :refer [fetch-grouped-sensor-data fetch-sensor-data]]
   [home-dashboard.utils :refer [format-sql-ts format-sql-ts-short real->str
                                 slurp-resource]]))

(defn sensor-data-rows [rows]
  (map
   (fn [e]
     (let [{temp :temperature
            humi :humidity
            date :date} e]
       [:tr {:class "bg-white border-b dark:bg-gray-800 dark:border-gray-800"}
        [:td {:class "px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white"} (format-sql-ts date)]
        [:td {:class "px-6 py-4"} (str (real->str temp) " °C")]
        [:td {:class "px-6 py-4"} (str (real->str humi) " %")]]))
   rows))

(defn get-more-rows-button-disabled []
  [:button#btn-get-more-rows
   {:class "cursor-not-allowed text-white bg-gray-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 dark:bg-gray-800"
    :disabled true}
   "Load more"])

(defn get-more-rows-button [limit offset]
  [:button#btn-get-more-rows
   {:hx-get (str "/api/get-more-rows?limit=" limit "&offset=" offset)
    :hx-swap "outerHTML"
    :class "text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 dark:bg-gray-800 dark:hover:bg-gray-700 dark:focus:ring-gray-700 dark:border-gray-700"}
   "Load more"])

(defn sensor-data-table [sensor-data]
  [:table {:class "w-full text-sm text-left text-gray-500 dark:text-gray-400"}
   [:thead {:class "text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400"}
    [:tr
     [:th.px-6.py-3 {:scope "col"} "date"]
     [:th.px-6.py-3 {:scope "col"} "temperature"]
     [:th.px-6.py-3 {:scope "col"} "humidity"]]]
   [:tbody#sensor-data-rows (concat (sensor-data-rows sensor-data))]])

(def interval-values
  [{:value 600 :text "10 minutes"}
   {:value 1800 :text "30 minutes"}
   {:value 3600 :text "1 hour"}
   {:value 21600 :text "6 hours"}
   {:value 43200 :text "12 hours"}
   {:value 86400 :text "1 day"}])

(def period-values
  [{:value 1 :text "last day"}
   {:value 7 :text "last week"}
   {:value 30 :text "last month"}
   {:value 180 :text "last 6 months"}
   {:value 365 :text "last year"}])

(defn sensor-chart [rows interval period]
  (let [chart-script (slurp-resource "public/sensor-chart.js")]
    [:div#chart-container {:class "relative w-full p-3 lg:w-1/2 lg:p-0"}
     [:canvas#chart]
     [:div.rounded.mt-1
      [:label {:for "select-interval"}]
      [:select#select-interval
       {:class "rounded p-1 bg-gray-900 text-gray-100 border border-gray-800"
        :name "interval"
        :autocomplete "off"
        :hx-get "/api/get-sensor-chart"
        :hx-target "#chart-container"
        :hx-include "[name='period']"
        :hx-swap "outerHTML"}
       (map (fn [v] [:option {:value (:value v) :selected (= (:value v) interval)} (:text v)]) interval-values)]
      [:label {:for "select-period" :class "ml-3"}]
      [:select#select-period
       {:class "rounded p-1 bg-gray-900 text-gray-100 border border-gray-800"
        :name "period"
        :autocomplete "off"
        :hx-get "/api/get-sensor-chart"
        :hx-target "#chart-container"
        :hx-include "[name='interval']"
        :hx-swap "outerHTML"}
       (map (fn [v] [:option {:value (:value v) :selected (= (:value v) period)} (:text v)]) period-values)]]
     [:script {:defer true}
      (h/raw
       (format chart-script
               (str/join "," (map #(str "\"" (format-sql-ts-short (:date %)) "\"") rows))
               (str/join "," (map :temperature rows))
               (str/join "," (map :humidity rows))))]]))

(defn home-page [_params]
  (let [limit 10
        offset 0
        rows (fetch-sensor-data 10 0)
        grouped-rows (fetch-grouped-sensor-data
                      (.minus (java.time.Instant/now) (java.time.Duration/ofDays 7))
                      (java.time.Instant/now)
                      3600)]
    (str
     (h/html
      (h/raw "<!DOCTYPE html>")
      [:html {:xmlns "http://www.w3.org/1999/xhtml" "xml:lang" "en" :lang "en" :class "dark"}
       [:head
        [:meta {:charset "UTF-8"}]
        [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
        [:meta {:name "htmx-config" :content "{\"useTemplateFragments\": \"true\"}"}]
        [:title "Sensor dashboard"]
        [:script {:src "https://unpkg.com/htmx.org@1.9.10/dist/htmx.min.js" :crossorigin "anonymous" :defer true}]
        [:script {:src "https://cdn.tailwindcss.com" :defer true}]
        [:script {:src "https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"}]
        [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" :rel "stylesheet"}]]
       [:body {:class "dark:bg-gray-900 bg-gray-100" :hx-boost "true"}
        [:div.mt-3.pl-3
         [:a {:href "/"}
          [:i {:class "fa fa-house text-gray-400 hover:text-gray-700"}]]]
        [:div
         [:div {:class "flex flex-col items-center justify-stretch lg:mb-5"}
          (sensor-chart grouped-rows 3600 7)]]
        [:div {:class "px-3 lg:px-0 flex flex-col md:justify-center md:items-center"}
         (if (empty? rows)
           [:div {:class "flex items-center justify-center"}
            [:img {:width "300px" :height "300px" :src "/undraw_void.png"}]]
           [:div {:class "overflow-auto rounded shadow-lg mb-5 w-full lg:w-1/2"}
            (sensor-data-table rows)])
         (get-more-rows-button limit offset)]]]))))

(defn api-get-more-rows [params]
  (let [limit (Integer/parseInt (get-in params [:params :limit] "10"))
        offset (Integer/parseInt (get-in params [:params :offset] "0"))
        rows (fetch-sensor-data limit offset)]
    {:status 200
     :headers {"Content-Type" "text/html" "Cache-Control" "public, max-age=300"}
     :body (str
            (h/html [:tbody {:hx-swap-oob "beforeend:#sensor-data-rows"} (sensor-data-rows rows)])
            (if (or (empty? rows) (< (count rows) limit))
              (h/html (get-more-rows-button-disabled))
              (h/html (get-more-rows-button limit (+ limit  offset)))))}))

(defn api-get-sensor-chart [params]
  (let [interval (Integer/parseInt (get-in params [:params :interval] "3600"))
        period (Integer/parseInt (get-in params [:params :period] "7"))
        rows (fetch-grouped-sensor-data
              (.minus (java.time.Instant/now) (java.time.Duration/ofDays period))
              (java.time.Instant/now)
              interval)]
    {:status 200
     :headers {"Content-Type" "text/html" "Cache-Control" "public, max-age=300"}
     :body (str
            (h/html
             (sensor-chart rows interval period)))}))

