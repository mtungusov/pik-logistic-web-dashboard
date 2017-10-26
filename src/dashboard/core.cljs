(ns dashboard.core
    (:require [rum.core :as rum]
              [datascript.core :as d]
              [dashboard.db.core :as db]
              [dashboard.api.core :as api]
              [dashboard.utils.core :refer [to-sec to-sec-from-str format-time format-sec now-int]]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello world!"}))

(defonce checked-zones (atom #{}))

(rum/defc checkbox < rum/reactive
  [label value *ref]
  (let [vals (rum/react *ref)
        checked (contains? vals value)]
    [:label
     [:input {:type "checkbox"
              :checked checked
              :value value
              :on-click (fn [_]
                          (if checked
                            (swap! *ref disj value)
                            (swap! *ref conj value))
                          (prn @*ref))}]
     label]))


(rum/defc geo-zones [zones]
  [:div {:class "card-body"}
   (for [z zones]
     [:div {:class "form-check" :key (:zone/id z)}
      [:label {:class "form-check-label"}
       [:input {:type "checkbox" :class "form-check-input" :value (:zone/label z)}]
       (:zone/label z)]])])


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


(defn set-time-class [dur]
  (cond
    (>= dur (* 120 60)) "badge badge-danger"
    (>= dur (* 60 60)) "badge badge-warning"
    :else "badge badge-light"))


(rum/defcs timer-from < (rum/local (now-int) ::now-key)
  [state time-str]
  (let [sec (to-sec-from-str time-str)
        now-atom (::now-key state)
        now-sec (to-sec @now-atom)
        dur (- now-sec sec)]
    (js/setTimeout #(reset! now-atom (now-int)) 10000)
    [:span {:class (set-time-class dur)} (format-sec dur)]))

;(rum/defc prev-zone-time [cur prev time]
;  (when (and (empty? cur) (not-empty prev))
;    [:span {:class "badge badge-secondary"} (format-time time)]))

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
        status (get-status (:tracker/status_movement tr))
        group-title (:tracker/group_title tr)]
    [:tr
     [:td {:class "tracker-label"}
          [:span {:class "group-title"} (str group-title ": ")]
          [:span tracker-label]]
     [:td [:span {:class "badge badge-light"} status]]
     (if-not (= label "я-вне-зон")
       [:td {:class "zone-label"} [:span label]]
       [:td [:span {:class "badge badge-secondary"}
             (str "Выезд: " prev-label)]])
     [:td (if-not (empty? label)
            (timer-from cur-event-time))]
     (if-not (= label "я-вне-зон")
       [:td [:span {:class "badge badge-light"} (format-time cur-event-time)]]
       [:td [:span {:class "badge badge-secondary"} (format-time cur-event-time)]])]))


(rum/defc trackers [trs]
  [:tbody
   (for [t trs]
     (tracker t))])


(rum/defc tablo [trs]
  [:table {:class "table table-sm"}
   (tracker-header)
   (trackers trs)])


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
;(db/zones @db/conn)

