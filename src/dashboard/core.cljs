(ns dashboard.core
    (:require [rum.core :as rum]
              [cljs-time.core :as t]
              [cljs-time.coerce :as tc]
              [cljs-time.format :as tf]
              [goog.string :as gstring]
              [clojure.string :as string]
              [datascript.core :as d]
              [dashboard.db.core :as db]
              [dashboard.api.core :as api]))

(enable-console-print!)

;(println "This text is printed from src/dashboard/core.cljs. Go ahead and edit it and see reloading in action.")
;
;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(rum/defc geo-zones [zones]
  [:ul {:class "card-body"}
   (for [z zones]
     [:li {:key (:zone/id z)} (:zone/label z)])])


(rum/defc transport-groups [groups]
  [:ul {:class "card-body"}
   (for [g groups]
     [:li {:key (:group/id g)} (:group/title g)])])


(rum/defc tracker-header []
  [:thead.thead-default
   [:tr
    [:th "Автомобиль"]
    [:th "Статус"]
    [:th "Геозона"]
    [:th "Время в зоне"]
    [:th "Въезд/Выезд"]]])


(defn to-sec [time-int]
  (js/Math.trunc (/ time-int 1000)))


(defn to-sec-from-str [time-str]
  (let [t (tf/parse-local time-str)]
    (to-sec t)))

(defn format-time [time-str]
  (let [t (tf/parse-local time-str)]
    (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm") t)))

;(format-time "2017-10-24 10:47:56")
;(require '[goog.string :as gstring])
;(require '[clojure.string :as string])
;(float 59)
;(gstring/format "%02d" 19)
;(string/join " " [1 2 3])


(def one-min 60)
(def one-hour (* 60 one-min))
(def one-day (* 24 one-hour))

;(js/Math.floor (/ 86401 one-day))
;(/ 86399 one-day)

(defn format-sec [sec]
  (let [dd (/ sec one-day)
        hh (/ (rem sec one-day) one-hour)
        mm (/ (rem (rem sec one-day) one-hour) one-min)
        ss (rem (rem (rem sec one-day) one-hour) one-min)
        in-day (string/join ":" (map (comp (partial gstring/format "%02d") js/Math.floor) [hh mm]))]
    (if (>= dd 1)
      (str (js/Math.floor dd) "д. " in-day)
      in-day)))


;(format-sec 12386400)
(defn set-time-class [dur]
  (cond
    (>= dur (* 120 60)) "badge badge-danger"
    (>= dur (* 60 60)) "badge badge-warning"
    :else "badge badge-light"))

(rum/defcs timer-from < (rum/local (tc/to-long (t/now)) ::now-key)
  [state time-str]
  (let [sec (to-sec-from-str time-str)
        now-atom (::now-key state)
        now-sec (to-sec @now-atom)
        dur (- now-sec sec)]
    (js/setTimeout #(reset! now-atom (tc/to-long (t/now))) 10000)
    [:span {:class (set-time-class dur)} (format-sec dur)]))

(rum/defc prev-zone-label [cur prev time]
  (when (and (empty? cur) (not-empty prev))
    [:div
     [:span {:class "badge badge-info"} "Выезд:"]
     [:span {:class "badge badge-secondary"} prev]]))

(rum/defc prev-zone-time [cur prev time]
  (when (and (empty? cur) (not-empty prev))
    [:span {:class "badge badge-secondary"} (format-time time)]))

(defn get-event-time [cur-time parent-time cur parent]
  (if (and (not-empty parent)
           (not-empty parent-time)
           (= parent cur))
    parent-time
    cur-time))

(defn get-status [status]
  (case status
    "parked" "стоит"
    "stopped" "стоит"
    "moving" "движ."
    status))

(rum/defc tracker < {:key-fn (fn [tr] (str (:tracker/id tr)))} [tr]
  (let [event-time (:tracker/event_time tr)
        parent-time (:tracker/last_parent_inzone_time tr)
        label (:tracker/zone_label_current tr)
        tracker-label (:tracker/label tr)
        parent-label (:tracker/zone_parent_label tr)
        prev-label (:tracker/zone_label_prev tr)
        cur-event-time (get-event-time event-time parent-time label parent-label)
        status (get-status (:tracker/status_movement tr))]
    [:tr
     [:td [:span {:class "badge badge-light"} tracker-label]]
     [:td [:span {:class "badge badge-light"} status]]
     [:td (if-not (empty? label)
            [:span {:class "badge badge-light"} label]
            (prev-zone-label label prev-label event-time))]
     [:td (if-not (empty? label)
            (timer-from cur-event-time))]
     [:td (if-not (empty? label)
            [:span {:class "badge badge-light"} (format-time cur-event-time)]
            (prev-zone-time label prev-label event-time))]]))


(rum/defc trackers [trs]
  [:tbody
   (for [t trs]
     (tracker t))])


(rum/defc tablo [trs]
  [:table {:class "table table-sm"}
   (tracker-header)
   (trackers trs)])


;(when-let [element (-> js/document (.getElementById "zones"))]
;  (rum/mount (geo-zones db/zones) element))

;(when-let [element (-> js/document (.getElementById "groups"))]
;  (rum/mount (transport-groups (db/groups db)) element))

(defn render [db]
  (when-let [element (-> js/document (.getElementById "zones"))]
    (rum/mount (geo-zones (db/zones db)) element))

  (when-let [element (-> js/document (.getElementById "groups"))]
    (rum/mount (transport-groups (db/groups db)) element))

  (when-let [element (-> js/document (.getElementById "tablo"))]
    (rum/mount (tablo (db/trackers db)) element)))

(d/listen! db/conn :render
  (fn [tx-report]
    (render (:db-after tx-report))))

(api/load-trackers)
(render @db/conn)

(defn on-js-reload [])
  ;(swap! app-state update-in [:__figwheel_counter] inc))

;(d/transact! db/conn [{:tracker/id "t2" :tracker/status_movement "stopped"}])
;(d/transact! db/conn [{:tracker/id "t0" :tracker/status_movement "moved"}])

;(db/groups @db/conn)

;(require '[cljs-time.coerce :as tc])
;(require '[cljs-time.format :as tf])
;(tc/from-long (tc/to-long (tf/parse-local "2017-10-19 15:48:00")))
;(tc/to-long (t/now))
;(t/now)
;(/ (tc/to-long (t/now)) 1000)
;
;(js/Date.)
;(t/now)
;(tc/from-long (tc/to-long "2017-10-19 15:21:00"))
;(t/to-utc-time-zone (tc/from-string "2017-10-19 15:21:00"))