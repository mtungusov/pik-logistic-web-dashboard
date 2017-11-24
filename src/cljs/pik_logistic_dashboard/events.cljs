(ns pik-logistic-dashboard.events
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [day8.re-frame.http-fx]
            [ajax.core :refer [json-request-format json-response-format]]
            [pik-logistic-dashboard.db :as db]
            [pik-logistic-dashboard.subs :as subs]))

(def base-url "https://dashboard-cars.pik-industry.ru")
(def api-version "api/v4")


(defn uri [& path]
  (string/join "/" (concat [base-url api-version] path)))

;(uri "q/trackers")


(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))


(rf/reg-event-fx
  ::load-data
  (fn [_ _]
    (rf/dispatch [::load-trackers])
    (rf/dispatch [::load-groups])
    (rf/dispatch [::load-geo-zones])))

(rf/reg-event-fx
  ::load-trackers
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/trackers")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::trackers-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-groups
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/groups")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::groups-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-geo-zones
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/zones")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::zones-loaded]
                  :on-failure [::error-api]}}))


(defn convert-tracker [tracker]
  (let [zone_label_in (:zone_label_in tracker)]
    (cond-> tracker
      zone_label_in (assoc :zone_label_cur zone_label_in)
      (nil? zone_label_in) (assoc :zone_label_cur "вне зон")
      true (assoc :order-comp "label + time in msec"))))


(rf/reg-event-db
  ::trackers-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      ;(js/console.log "loaded: " (str (extract-groups items)))
      ;(rf/dispatch [::load-groups items])
      ;(rf/dispatch [::load-geo-zones items])
      (assoc db :trackers (map convert-tracker items)))))


(rf/reg-event-db
  ::groups-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :groups (set items)))))

(rf/reg-event-db
  ::zones-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :geo-zones (set items)))))


(rf/reg-event-db
  ::error-api
  (fn [db _]
    (js/console.log "API error!")
    db))


(rf/reg-event-db
  ::check-one-checkbox
  (fn [db [_ db-key value]]
    (update db db-key conj value)))


(rf/reg-event-db
  ::uncheck-one-checkbox
  (fn [db [_ db-key value]]
    (update db db-key disj value)))


(rf/reg-event-db
  ::clear-checkbox-selections
  (fn [db [_ db-key]]
    (assoc db db-key #{})))


(rf/reg-event-db
  ::invert-geo-zones-selections
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/geo-zones]))]
      (update db :geo-zones-selected #(clojure.set/difference all-items %)))))

(rf/reg-event-db
  ::invert-groups-selections
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/groups]))]
      (update db :groups-selected #(clojure.set/difference all-items %)))))
