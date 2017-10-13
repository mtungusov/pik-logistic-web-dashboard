(ns dashboard.core
    (:require [rum.core :as rum]))

(enable-console-print!)

(println "This text is printed from src/dashboard/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(rum/defc geo-zones [zones]
  [:ul
   (for [z zones]
     [:li {:key (:id z)} (:label z)])])

(rum/defc transport-groups [groups]
  [:ul
   (for [g groups]
     [:li {:key (:id g)} (:title g)])])

(rum/defc tracker [tr]
  [:.tracker {:key (:id tr)}
   [:span (:label tr)]
   [:span (:status_movement tr)]
   [:span (:event_time tr)]
   [:span (:zone_label_current tr)]
   [:span (:event_time tr)]
   [:span (:event_time tr)]])

(rum/defc tablo [trackers]
  [:#tablo
   [:.header
    [:span "Автомобиль"]
    [:span "Статус"]
    [:span "Интервал"]
    [:span "Геозона"]
    [:span "Время в зоне"]
    [:span "Въезд"]]
   (for [t trackers]
     (tracker t))])


(def t-zones [{:id 0 :label "480 КЖИ - погр."}
              {:id 1 :label "Балашиха - разгр."}
              {:id 2 :label "Боброво - разгр."}
              {:id 3 :label "Бунинские луга - разгр."}
              {:id 4 :label "Ново-Куркино (Химки) - разгр."}])

(def t-groups [{:id 0 :title "-Инлоудер"}
               {:id 1 :title "-Лангендорф"}
               {:id 2 :title "-Панелевоз У-148 12,5т"}
               {:id 3 :title "Погрузчики и технологические"}])

(def t-trackers [{:id 0 :label "о230ес50 лангендорф Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id 1 :label "у901еу750 У-230 Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id 2 :label "с192во777 борт 20т 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
                 {:id 3 :label "а446хв77 У-230 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}])

(rum/defc app []
  [:.content
   [:.zones (geo-zones t-zones)]
   [:.groups (transport-groups t-groups)]
   (tablo t-trackers)])

(rum/mount (app)
           (-> js/document (.getElementById "app")))

(defn on-js-reload [])
  ;(swap! app-state update-in [:__figwheel_counter] inc))
