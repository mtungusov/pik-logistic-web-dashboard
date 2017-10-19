(ns dashboard.core
    (:require [rum.core :as rum]))

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


(rum/defc tracker < {:key-fn (fn [tr] (str (:id tr)))} [tr]
  [:tr
   [:td (:label tr)]
   [:td (:status_movement tr)]
   [:td (:zone_label_current tr)]
   [:td "08:20"]
   [:td (:event_time tr)]])

(rum/defc trackers [trs]
  [:tbody
   (for [t trs]
     (tracker t))])

(rum/defc tablo [trs]
  [:table {:class "table table-sm"}
   (tracker-header)
   (trackers trs)])


(def t-zones [{:id "z0" :label "480 КЖИ - погр."}
              {:id "z1" :label "Балашиха - разгр."}
              {:id "z2" :label "Боброво - разгр."}
              {:id "z3" :label "Бунинские луга - разгр."}
              {:id "z4" :label "Ново-Куркино (Химки) - разгр."}])

(def t-groups [{:id "g0" :title "-Инлоудер"}
               {:id "g1" :title "-Лангендорф"}
               {:id "g2" :title "-Панелевоз У-148 12,5т"}
               {:id "g3" :title "Погрузчики и технологические"}])

(def t-trackers [{:id "t0" :label "о230ес50 лангендорф Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id "t1" :label "у901еу750 У-230 Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id "t2" :label "с192во777 борт 20т 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id "t3" :label "а446хв77 У-230 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}])


;(rum/defc app []
;  [:#content
;   [:#zones (geo-zones t-zones)]
;   [:#groups (transport-groups t-groups)]
;   [:#tablo
;    (tracker-header)
;    (trackers t-trackers)]])


;(when-let [element (-> js/document (.getElementById "app"))]
;  (rum/mount (app) element))

(when-let [element (-> js/document (.getElementById "zones"))]
  (rum/mount (geo-zones t-zones) element))

(when-let [element (-> js/document (.getElementById "groups"))]
  (rum/mount (transport-groups t-groups) element))

(when-let [element (-> js/document (.getElementById "tablo"))]
  (rum/mount (tablo t-trackers) element))

(defn on-js-reload [])
  ;(swap! app-state update-in [:__figwheel_counter] inc))
