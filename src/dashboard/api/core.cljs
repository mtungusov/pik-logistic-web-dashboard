(ns dashboard.api.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [datascript.core :as d]
            [dashboard.config :refer [api-host]]
            [dashboard.utils.core :refer [nil-to-str]]
            [dashboard.db.core :as db]))


;(def api-host "http://localhost:3000/api/v2")
;
;{:group_id 112474, :event "outzone", :zone_label_prev "ДСК-Град - погр.", :label "н552ке750 борт 20т ДСК-Град", :id 191404, :status_connection "active", :zone_label_current nil, :zone_id 94608, :zone_parent_id nil, :event_time "2017-10-23 08:18:19", :last_parent_inzone_time nil, :status_movement "parked", :group_title "Борт 12т.-20т.", :zone_label "ДСК-Град - погр."}

;(defn nil-to-str [str]
;  (if (nil? str) "" str))


(defn convert-tracker-for-db [tracker order-atom]
  (let [id (str "t-"(:id tracker))
        label (:label tracker)
        status_movement (:status_movement tracker)
        event_time (:event_time tracker)
        zone_label_current (:zone_label_current tracker)
        zone_label_prev (:zone_label_prev tracker)
        zone_parent_label (:zone_parent_label tracker)
        last_parent_inzone_time (:last_parent_inzone_time tracker)]
    (swap! order-atom inc)
    {:tracker/id (str id)
     :tracker/order @order-atom
     :tracker/label label
     :tracker/status_movement status_movement
     :tracker/event_time event_time
     :tracker/zone_label_current (nil-to-str zone_label_current)
     :tracker/zone_label_prev (nil-to-str zone_label_prev)
     :tracker/zone_parent_label (nil-to-str zone_parent_label)
     :tracker/last_parent_inzone_time (nil-to-str last_parent_inzone_time)}))


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

(defn datom-from-one-api-element [el order-atom]
  (remove nil? [(convert-tracker-for-db el order-atom)
                (convert-group-for-db el)
                (convert-zone-for-db el)]))

;(datom-from-one-api-element {:zone_parent_label nil, :group_id 112479, :event "inzone", :zone_label_prev nil, :label "е774ор777 легковой ПИ", :id 209286, :status_connection "active", :zone_label_current "480 КЖИ - погр.", :zone_id 96909, :zone_parent_id nil, :event_time "2017-10-24 13:01:26", :last_parent_inzone_time nil, :status_movement "parked", :group_title "Легковой автомобиль", :zone_label "480 КЖИ - погр."} (atom 1))

(defn load-trackers []
  (go (let [url (str api-host "/q/trackers")
            resp (<! (http/get url))
            status (:status resp)]
        (if (= status 200)
          (let [trackers (get-in resp [:body :result])
                trackers-order (atom 0)]
                ;datoms (reduce #(into %1 (datom-from-one-api-element %2 trackers-order)) [] trackers)]
            ;(doseq [d datoms] (prn d)))
            (d/transact! db/conn (reduce #(into %1 (datom-from-one-api-element %2 trackers-order)) [] trackers)))
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

