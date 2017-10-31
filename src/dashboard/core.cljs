(ns dashboard.core
    (:require [rum.core :as rum]
              [datascript.core :as d]
              [dashboard.db.core :as db]
              [dashboard.api.core :as api]
              [dashboard.utils.core :refer [to-sec to-sec-from-str format-time format-sec now-int set-to-str]]))

(enable-console-print!)

;(defonce app-state (atom {:text "Hello world!"}))
(defonce checked-zones (atom #{}))
(defonce checked-groups (atom #{}))

(defonce zones-ref-name "dashboard/selected-zones")
(defonce groups-ref-name "dashboard/selected-groups")

(case "dashboard/selected-zones" zones-ref-name "T" "f")

(defn save-checked [*ref ref-name]
  (let [save-str (JSON.stringify (prn-str @*ref))]
    (js/localStorage.setItem ref-name save-str)))


(defn validate-checked-zones [val]
  (let [fresh (set (map :zone/label (db/zones @db/conn)))]
    (clojure.set/intersection val fresh)))


(defn validate-checked-groups [val]
  (let [fresh (set (map :group/title (db/groups @db/conn)))]
    (clojure.set/intersection val fresh)))


(defn validate-checked [val ref-name]
  (case ref-name
    "dashboard/selected-zones" (validate-checked-zones val)
    "dashboard/selected-groups" (validate-checked-groups val)))

;(validate-checked #{"480 КЖИ - погр."} "dashboard/selected-zones")
;(validate-checked #{"-Тюльпан"} "dashboard/selected-groups")

(defn load-checked [*ref ref-name]
  (let [load-str (JSON.parse (js/localStorage.getItem ref-name))
        val (cljs.reader/read-string load-str)
        to-load (validate-checked val ref-name)]
    (reset! *ref to-load)))


;(load-checked checked-zones zones-ref-name)
;(load-checked checked-groups groups-ref-name)

(defn reset-checked-items [checked-atom]
  (reset! checked-atom #{}))

(defn invert-checked-items [attr items checked-atom]
  (let [all-items (set (map (keyword attr) items))
        diffs (clojure.set/difference all-items @checked-atom)]
    (reset! checked-atom diffs)))


(rum/defc status-zones < rum/reactive
  []
  (let [zones (rum/react checked-zones)]
    [:div#status-zones {:class (when (empty? zones) "d-none")}
     [:span {:class "text-info"} "Геозоны: "]
     (set-to-str zones)]))


(rum/defc status-groups < rum/reactive
  []
  (let [groups (rum/react checked-groups)]
    [:div#status-groups {:class (when (empty? groups) "d-none")}
     [:span {:class "text-info"} "Группы: "]
     (set-to-str groups)]))


(rum/defc show-status []
  [:div
   (status-zones)
   (status-groups)])

(rum/defc checkbox < rum/reactive
  [label value *ref ref-name]
  (let [checked (contains? (rum/react *ref) value)]
    [:label {:class "form-check-label"}
     [:input {:type "checkbox"
              :class "form-check-input"
              :checked checked
              :value value
              :on-click (fn [_]
                          (if checked
                            (swap! *ref disj value)
                            (swap! *ref conj value))
                          (save-checked *ref ref-name))}]
     label]))


(rum/defc geo-zones [zones]
  [:div {:class "card-body"}
   (for [z zones]
     [:div {:class "form-check" :key (:zone/id z)}
      (let [value (:zone/label z)]
        (checkbox value value checked-zones zones-ref-name))])])
   ;[:div {:class "form-check" :key (:zone/id "z-0")}
   ; (checkbox "вне зон" "вне зон" checked-zones)]])


(rum/defc geo-zones-card [items]
  [:div.card
   [:div.card-header {:role "tab"}
    [:a.collapsed {:href "#zones"
                   :data-toggle "collapse"
                   :aria-expanded "false"
                   :aria-controls "zones"}
     [:span.oi.oi-globe]
     [:span "Геозоны"]]]
   [:div#zones.collapse {:role "tabpanel" :data-parent "#accordion"}
    [:div.btn-toolbar {:role "toolbar"}
     [:div.btn-group {:role "group"}
      [:button.btn.btn-sm.btn-outline-danger {:type "button"}
       [:span.oi.oi-ban {:on-click (fn [_]
                                     (reset-checked-items checked-zones)
                                     (save-checked checked-zones zones-ref-name))}]]
      [:button.btn.btn-sm.btn-outline-primary {:type "button"}
       [:span.oi.oi-loop-circular {:on-click (fn [_]
                                               (invert-checked-items "zone/label" items checked-zones)
                                               (save-checked checked-zones zones-ref-name))}]]]]
    (geo-zones items)]])


(rum/defc transport-groups [groups]
  [:div {:class "card-body"}
   (for [g groups]
     [:div {:class "form-check" :key (:group/id g)}
      (let [value (:group/title g)]
        (checkbox value value checked-groups groups-ref-name))])])


(rum/defc transport-groups-card [items]
  [:div.card
   [:div.card-header {:role "tab"}
    [:a.collapsed {:href "#groups"
                   :data-toggle "collapse"
                   :aria-expanded "false"
                   :aria-controls "groups"}
     [:span.oi.oi-grid-three-up]
     [:span "Группы транспорта"]]]
   [:div#groups.collapse {:role "tabpanel" :data-parent "#accordion"}
    [:div.btn-toolbar {:role "toolbar"}
     [:div.btn-group {:role "group"}
      [:button.btn.btn-sm.btn-outline-danger {:type "button"}
       [:span.oi.oi-ban {:on-click (fn [_]
                                     (reset-checked-items checked-groups)
                                     (save-checked checked-zones groups-ref-name))}]]
      [:button.btn.btn-sm.btn-outline-primary {:type "button"}
       [:span.oi.oi-loop-circular {:on-click (fn [_]
                                               (invert-checked-items "group/title" items checked-groups)
                                               (save-checked checked-zones groups-ref-name))}]]]]
    (transport-groups items)]])


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


;(rum/defcs timer-from < (rum/local (now-int) ::now-key)
;  [state time-str]
;  (let [sec (to-sec-from-str time-str)
;        now-atom (::now-key state)
;        now-sec (to-sec @now-atom)
;        dur (- now-sec sec)]
;    (js/setTimeout #(reset! now-atom (now-int)) 15000)
;    [:span {:class (set-time-class dur)} (format-sec dur)]))


;(rum/defc timer-from < rum/reactive
;  [time-str]
;  (let [sec (to-sec-from-str time-str)
;        now-sec (to-sec (rum/react now-atom))
;        dur (- now-sec sec)]
;    [:span {:class (set-time-class dur)} (format-sec dur)]))
;

(rum/defcs timer-from < (rum/local (now-int) ::now-key)
  [state time-in-sec]
  (let [now-atom (::now-key state)
        dur (- @now-atom time-in-sec)]
    (js/setInterval #(swap! now-atom now-int) 9000)
    [:span {:class (set-time-class dur)} (format-sec dur)]))


;(defn get-event-time [cur-time parent-time cur parent]
;  (if (and (not-empty parent)
;           (not-empty parent-time)
;           (= parent cur))
;    parent-time
;    cur-time))

;(defn get-status [status]
;  (case status
;    "parked" "стоит"
;    "stopped" "стоит"
;    "moving" "движ."
;    status))


;(rum/defc tracker-label < rum/static [tracker group]
;  [:td {:class "tracker-label"}
;   [:span {:class "badge badge-primary"}]
;   [:span {:class "group-title"} (str group ": ")]
;   [:span tracker]])
;
;(rum/defc tracker-status < rum/static [status]
;  [:td [:span {:class "badge badge-light"} status]])
;
;(rum/defc tracker-zone < rum/static [zone zone-prev]
;  (case zone
;    "вне зон" [:td [:span {:class "badge badge-secondary"} (str "Выезд: " zone-prev)]]
;    [:td {:class "zone-label"} [:span zone]]))
;
;(rum/defc tracker-time-in-zone < rum/static [event-time]
;  [:td (timer-from event-time)])
;
;(rum/defc tracker-event-time < rum/static [zone event-time]
;  [:td [:span
;        {:class (case zone "вне зон" "badge badge-secondary" "badge badge-light")}
;        (format-time event-time)]])

;(defn tracker-visible? [zone group checked-zones checked-groups]
;  (case [(empty? checked-zones) (empty? checked-groups) (contains? checked-zones zone) (contains? checked-groups group)]
;    [true true false false] true
;    [false true false false] false
;    [false true true false] true
;    [true false false false] false
;    [true false false true] true
;    [false false false false] false
;    [false false true false] false
;    [false false false true] false
;    [false false true true] true
;    false))


;(rum/defc tracker < {:key-fn (fn [tr] (:tracker/id tr))}
;                    rum/static
;  [tr]
;  (let [event-time (:tracker/event_time tr)
;        parent-time (:tracker/last_parent_inzone_time tr)
;        tracker (:tracker/label tr)
;        zone-label (:tracker/zone_label_current tr)
;        zone-label-parent (:tracker/zone_parent_label tr)
;        zone-label-prev (:tracker/zone_label_prev tr)
;        cur-event-time (get-event-time event-time parent-time zone-label zone-label-parent)
;        pr-cur-event-time (format-time cur-event-time)
;        status (get-status (:tracker/status_movement tr))
;        group-title (:tracker/group_title tr)]
;        ;vis? (tracker-visible? zone-label group-title (rum/react checked-zones) (rum/react checked-groups))]
;    ;(if vis?
;      [:tr
;       ;(tracker-label tracker group-title)
;       [:td {:class "tracker-label"}
;            [:span {:class "group-title"} group-title]
;            [:span tracker]]
;       ;(tracker-status status)
;       [:td [:span {:class "badge badge-light"} status]]
;       ;(tracker-zone zone-label zone-label)
;       (if-not (= zone-label "вне зон")
;         [:td {:class "zone-label"} [:span zone-label]]
;         [:td [:span {:class "badge badge-secondary"}
;               (str "Выезд: " zone-label)]])
;       ;(tracker-time-in-zone cur-event-time)
;       [:td (if-not (empty? zone-label) (timer-from cur-event-time))]
;       ;(tracker-event-time zone-label event-time)]))
;       [:td [:span
;             {:class (case zone-label "вне зон" "badge badge-secondary" "badge badge-light")}
;             pr-cur-event-time]]]))
;      ;[:tr {:class "d-none"}])))


;(rum/defc trackers < rum/reactive
;  [db]
;  (let [trs (db/trackers db (rum/react checked-zones) (rum/react checked-groups))]
;    [:tbody
;     (mapv tracker trs)]))
;   ;(for [t trs]
;   ;  (tracker t))])

(rum/defc zone-label < rum/static
  [label label-prev]
  (case label
    "вне зон" [:td [:span {:class "badge badge-secondary"} (str "Выезд: " label-prev)]]
    [:td {:class "zone-label"} [:span label]]))


(rum/defc tr-simple < rum/static
                      {:key-fn (fn [tr] (:tracker/id tr))}
  [i]
  (let [t-label  (:tracker/label i)
        ;t-order  (:tracker/order-comp i)
        g-title  (:tracker/group_title i)
        t-status (:tracker/status_movement i)
        z-label  (:tracker/zone_label_current i)
        z-label-prev (:tracker/zone_label_prev i)
        e-time   (:tracker/event_time i)
        e-time-utc-in-sec (:tracker/event_time_in_sec i)]
    [:tr
     [:td {:class "tracker-label"}
      ;[:span {:class "badge badge-warning"} t-order]
      [:span {:class "group-title"} (str g-title ": ")]
      [:span t-label]]
     [:td [:span {:class "badge badge-light"} t-status]]
     (zone-label z-label z-label-prev)
     [:td (timer-from e-time-utc-in-sec)]
     [:td [:span
           {:class (case z-label "вне зон" "badge badge-secondary" "badge badge-light")}
           (format-time e-time)]]]))


(rum/defc trackers < rum/static
  [items]
  [:tbody
   (mapv tr-simple items)])
   ;(for [_ items]
   ;  [:tr
   ;   [:td "tracker"]
   ;   [:td "status"]
   ;   [:td "geo"]
   ;   [:td "time in zone"]
   ;   [:td "event time"]])])
   ;  (tracker t))])


(rum/defc tablo < rum/reactive
  [db]
  (let [items (db/trackers db (rum/react checked-zones) (rum/react checked-groups))]
    [:table {:class "table table-sm"}
     (tracker-header)
     (trackers items)]))
  ;(trackers db)])
  ;(let [zones (rum/react checked-zones)
  ;      groups (rum/react checked-groups)
  ;      items (db/trackers db #{} #{})]
  ;  [:table {:class "table table-sm"}
  ;   (tracker-header)]))


(defn render-tablo [db]
  (when-let [element (-> js/document (.getElementById "tablo"))]
    (rum/mount (tablo db) element)))


(defn render-zones [items]
  (when-let [element (-> js/document (.getElementById "geo-zones"))]
    (rum/mount (geo-zones-card items) element)))


(defn render-groups [items]
  (when-let [element (-> js/document (.getElementById "transport-groups"))]
    (rum/mount (transport-groups-card items) element)))


(defn render-status []
  (when-let [element (-> js/document (.getElementById "status"))]
     (rum/mount (show-status) element)))


(defn render [db]
  (let [zones (db/zones db)
        groups (db/groups db)]
        ;items (db/trackers db zones groups)]
        ;items (db/trackers db #{} #{})]
    (render-zones zones)
    (render-groups groups)
    (render-status)
    (render-tablo db)))


(d/listen! db/conn :render
  (fn [tx-report]
    (load-checked checked-zones zones-ref-name)
    (load-checked checked-groups groups-ref-name)
    (render (:db-after tx-report))))

(api/load-trackers)
;(render @db/conn)



(js/setInterval #(api/load-trackers) (* 60 1000))

;(defn on-js-reload []
;  (swap! app-state update-in [:__figwheel_counter] inc))


;(db/groups @db/conn)
;(db/zones @db/conn)
;(def z (db/zones @db/conn))
;(:zone/label (first z))
;(clojure.set/intersection #{"вне зонqw" "some zone"} (set (map :zone/label (db/zones @db/conn))))
;(db/trackers @db/conn @checked-zones @checked-groups)
;(identity @db/conn)
;(identity @checked-zones)
;(prn "test")
