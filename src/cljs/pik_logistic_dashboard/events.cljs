(ns pik-logistic-dashboard.events
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [day8.re-frame.http-fx]
            [ajax.core :refer [json-request-format json-response-format]]
            [pik-logistic-dashboard.db :as db]))

(def base-url "https://dashboard-cars.pik-industry.ru")
(def api-version "api/v3")


(defn uri [& path]
  (string/join "/" (concat [base-url api-version] path)))

;(uri "q/trackers")


(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))


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

;(rf/dispatch [::load-trackers])

(defn- extract-data [trackers extract-key]
  (set (map extract-key trackers)))

(rf/reg-event-db
  ::trackers-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      ;(js/console.log "loaded: " (str (extract-groups items)))
      (rf/dispatch [::load-groups items])
      (rf/dispatch [::load-geo-zones items])
      (assoc db :trackers items))))


(rf/reg-event-db
  ::load-groups
  (fn [db [_ items]]
    (assoc db :groups (extract-data items :group_title))))

(rf/reg-event-db
  ::load-geo-zones
  (fn [db [_ items]]
    (assoc db :geo-zones (extract-data items :zone_label))))

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
    (let [new-set (disj (db-key db) value)]
      (update db db-key disj value))))
