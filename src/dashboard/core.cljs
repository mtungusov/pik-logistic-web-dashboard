(ns dashboard.core
    (:require [rum.core :as rum]
              [cljs-time.core :as t]
              [cljs-time.coerce :as tc]
              [cljs-time.format :as tf]
              [goog.string :as gstring]
              [clojure.string :as string]
              [dashboard.db.core :as db]))

(enable-console-print!)

;(println "This text is printed from src/dashboard/core.cljs. Go ahead and edit it and see reloading in action.")
;
;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(rum/defc geo-zones [zones]
  [:ul {:class "card-body"}
   (for [z zones]
     [:li {:key (:id z)} (:label z)])])


(rum/defc transport-groups [groups]
  [:ul {:class "card-body"}
   (for [g groups]
     [:li {:key (:id g)} (:title g)])])


(rum/defc tracker-header []
  [:thead.thead-default
   [:tr
    [:th "Автомобиль"]
    [:th "Статус"]
    [:th "Геозона"]
    [:th "Время в зоне"]
    [:th "Въезд"]]])


(defn to-sec [time-int]
  (js/Math.trunc (/ time-int 1000)))


(defn to-sec-from-str [time-str]
  (let [t (tf/parse-local time-str)]
    (to-sec t)))

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
        in-day (string/join ":" (map (comp (partial gstring/format "%02d") js/Math.floor) [hh mm ss]))]
    (if (>= dd 1)
      (str (js/Math.floor dd) "д. " in-day)
      in-day)))


;(format-sec 12386400)

(rum/defcs timer-from < (rum/local (tc/to-long (t/now)) ::now-key)
  [state time-str]
  (let [sec (to-sec-from-str time-str)
        now-atom (::now-key state)
        now-sec (to-sec @now-atom)
        dur (- now-sec sec)]
    [:span (format-sec dur)]))


(rum/defc tracker < {:key-fn (fn [tr] (str (:id tr)))} [tr]
  [:tr
   [:td (:label tr)]
   [:td (:status_movement tr)]
   [:td (:zone_label_current tr)]
   [:td (timer-from (:event_time tr))]
   [:td (:event_time tr)]])


(rum/defc trackers [trs]
  [:tbody
   (for [t trs]
     (tracker t))])


(rum/defc tablo [trs]
  [:table {:class "table table-sm"}
   (tracker-header)
   (trackers trs)])


(when-let [element (-> js/document (.getElementById "zones"))]
  (rum/mount (geo-zones db/zones) element))

(when-let [element (-> js/document (.getElementById "groups"))]
  (rum/mount (transport-groups db/groups) element))

(when-let [element (-> js/document (.getElementById "tablo"))]
  (rum/mount (tablo db/trackers) element))

(defn on-js-reload [])
  ;(swap! app-state update-in [:__figwheel_counter] inc))

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