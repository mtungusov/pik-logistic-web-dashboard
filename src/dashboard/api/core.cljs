(ns dashboard.api.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [datascript.core :as d]
            [dashboard.config :refer [api-host]]
            [dashboard.utils.core :refer [nil-to-str nil-label to-sec now-int]]
            [dashboard.db.core :as db]))


;(def api-host "http://localhost:3000/api/v2")
;
;{:group_id 112474, :event "outzone", :zone_label_prev "ДСК-Град - погр.", :label "н552ке750 борт 20т ДСК-Град", :id 191404, :status_connection "active", :zone_label_current nil, :zone_id 94608, :zone_parent_id nil, :event_time "2017-10-23 08:18:19", :last_parent_inzone_time nil, :status_movement "parked", :group_title "Борт 12т.-20т.", :zone_label "ДСК-Град - погр."}

;(defn nil-to-str [str]
;  (if (nil? str) "" str))


(defn get-event-time [event-time event-time-parent zone-label zone-label-parent]
  (if (and (not-empty zone-label-parent)
           (not-empty event-time-parent)
           (= zone-label-parent zone-label))
    event-time-parent
    event-time))

(defn get-status [status]
  (case status
    "parked" "стоит"
    "stopped" "стоит"
    "moving" "движ."
    status))

(defn zone-label-for-order [label]
  (case label
    "вне зон" "ЯЯ"
    label))

(defn convert-tracker-for-db [tracker]
  (let [id (str "t-"(:id tracker))
        t-label (:label tracker)
        event_time (:event_time tracker)
        last_parent_inzone_time (nil-to-str (:last_parent_inzone_time tracker))
        zone_label_current (nil-label (:zone_label_current tracker))
        zone_label_parent (nil-to-str (:zone_parent_label tracker))
        zone_label_prev (nil-to-str (:zone_label_prev tracker))
        status_last_update_time (:status_last_update tracker)
        status_movement (get-status (:status_movement tracker))
        status_connection (:status_connection tracker)
        group_title (:group_title tracker)
        event_time_cur (get-event-time event_time last_parent_inzone_time zone_label_current zone_label_parent)
        event_time_cur_in_sec (to-sec (js/Date.parse event_time_cur))
        t-now (now-int)]
    ;(swap! order-atom inc)
    {:tracker/id id
     ;:tracker/order @order-atom
     :tracker/label t-label
     :tracker/event_time event_time_cur
     :tracker/event_time_in_sec event_time_cur_in_sec
     ;:tracker/status_last_update_time status_last_update_time
     :tracker/status_movement status_movement
     :tracker/status_connection status_connection
     :tracker/zone_label_current zone_label_current
     :tracker/zone_label_prev zone_label_prev
     ;:tracker/zone_parent_label zone_label_parent
     ;:tracker/last_parent_inzone_time last_parent_inzone_time
     :tracker/group_title group_title
     :tracker/order-comp (str (zone-label-for-order zone_label_current) ":" event_time_cur_in_sec)}))

;cur-event-time (get-event-time event-time parent-time zone-label zone-label-parent)
;(defn get-event-time [cur-time parent-time cur parent]
;  (if (and (not-empty parent)
;           (not-empty parent-time)
;           (= parent cur))
;    parent-time
;    cur-time))



(defn convert-zone-for-db [zone]
  (let [id (str "z-"(:zone_id zone))
        label (:zone_label zone)]
    (when-not (or (nil? label) (nil? id))
      {:zone/id id
       :zone/label label})))

;(convert-zone-for-db {:zone_id 0 :zone_label "one"})

(defn convert-group-for-db [group]
  (let [id (str "g-"(:group_id group))
        title (:group_title group)]
    (when-not (or (nil? title) (nil? id))
      {:group/id id
       :group/title title})))

;(convert-group-for-db {:group_id 0 :group_title "zero"})

(defn datom-from-one-api-element [el]
  (remove nil? [(convert-tracker-for-db el)
                (convert-group-for-db el)
                (convert-zone-for-db el)]))

;(datom-from-one-api-element {:zone_parent_label nil, :group_id 112479, :event "inzone", :zone_label_prev nil, :label "е774ор777 легковой ПИ", :id 209286, :status_connection "active", :zone_label_current "480 КЖИ - погр.", :zone_id 96909, :zone_parent_id nil, :event_time "2017-10-24 13:01:26", :last_parent_inzone_time nil, :status_movement "parked", :group_title "Легковой автомобиль", :zone_label "480 КЖИ - погр."} (atom 1))

(defn load-trackers []
  ;(prn "load from DB")
  (go (let [url (str api-host "/q/trackers")
            resp (<! (http/get url))
            status (:status resp)]
        (if (= status 200)
          (let [trackers (get-in resp [:body :result])]
                ;trackers-order (atom 0)]
                ;datoms (reduce #(into %1 (datom-from-one-api-element %2 trackers-order)) [] trackers)]
            ;(doseq [d datoms] (prn d)))
            (d/transact! db/conn (reduce #(into %1 (datom-from-one-api-element %2)) [] trackers)))
            ;(prn (reduce #(into %1 (prn %2 trackers-order)) [] trackers)))
            ;(d/transact! db/conn datoms))
            ;(d/transact! db/conn (reduce #(conj %1 (convert-tracker-for-db %2 trackers-order)) [] trackers)))
            ;(doseq [t trackers] (prn t)))
            ;(doseq [t trackers] (prn (convert-tracker-for-db t trackers-order))))
          (js/alert "Ошибка связи с API")))))
        ;(prn (map :event trackers)))))

;(db/trackers @db/conn)
;(identity @db/conn)
;(go (let [url (str api-host "/q/trackers")
;          resp (<! (http/get url))]
;      (prn resp)))

;(def url (str api-host "/q/trackers"))
;(def resp-0 (http/get url))
;(go (let [resp (<! (identity resp-0))]
;      (prn resp)))

;(load-trackers)

