(ns dashboard.db.core
  (:require [datascript.core :as d]))

(def schema {:tracker/id {:db/unique :db.unique/identity}
             :tracker/label {:db/unique :db.unique/identity}
             :tracker/status_movement {}
             :tracker/event_time {:db/index true}
             :tracker/zone_label_current {:db/index true}

             :zone/id {:db/unique :db.unique/identity}
             :zone/label {:db/index true}

             :group/id {:db/unique :db.unique/identity}
             :group/title {:db/index true}})

(defonce conn (d/create-conn schema))
;(identity conn)

;(def datoms [{:tracker/id "t0"
;              :tracker/label "о230ес50 лангендорф Щелково"
;              :tracker/status_movement "offline"
;              :tracker/event_time "2017-10-20 16:10:00"
;              :tracker/zone_label_current "480 КЖИ - погр."}
;
;             {:tracker/id "t1"
;              :tracker/label "у901еу750 У-230 Щелково"
;              :tracker/status_movement "parked"
;              :tracker/event_time "2017-10-20 16:15:00"
;              :tracker/zone_label_current "480 КЖИ - разг."}
;
;             {:tracker/id "t2"
;              :tracker/label "с192во777 борт 20т 1АК"
;              :tracker/status_movement "parked"
;              :tracker/event_time "2017-10-23 13:20:00"
;              :tracker/zone_label_current "480 КЖИ - погр."}])


;(d/transact! conn datoms)



;(d/transact! conn [{:tracker/id "t2" :tracker/status_movement "stopped"}])

;(def ids (d/q '[:find ?e
;                :where [?e :tracker/id]]
;           db))

;(d/q '[:find [?e ...]
;       :where [?e :tracker/id]]
;     @conn)

;(d/db? db)
;(map first ids)

;(map (d/entity @conn 3) [:tracker/id :tracker/label :tracker/event_time :tracker/zone_label_current])

;(d/q '[:find [?l ...]
;       :where [?e :tracker/id ?i]
;              [?e :tracker/zone_label_current ?l]]
;     @conn)

(defn trackers [db]
  (let [q '[:find [?e ...]
                :where [?e :tracker/id]]
        ids (d/q q db)
        keys [:tracker/id :tracker/order :tracker/label :tracker/status_movement :tracker/event_time :tracker/zone_label_current :tracker/zone_label_prev :tracker/zone_parent_label :tracker/last_parent_inzone_time]]
    (sort-by :tracker/order (reduce #(conj %1 (zipmap keys (map (d/entity db %2) keys))) [] ids))))

;(d/q '[:find [?e ...]
;       :where [?e :zone/id]]
;     @conn)

(defn groups [db]
  (let [q '[:find [?e ...]
            :where [?e :group/id]]
        ids (d/q q db)
        keys [:group/id :group/title]]
    (sort-by :group/title
      (reduce #(conj %1 (zipmap keys (map (d/entity db %2) keys))) [] ids))))

;(groups @conn)

(defn zones [db]
  (let [q '[:find [?e ...]
            :where [?e :zone/id]]
        ids (d/q q db)
        keys [:zone/id :zone/label]]
    (sort-by :zone/label
      (reduce #(conj %1 (zipmap keys (map (d/entity db %2) keys))) [] ids))))

;(zones @conn)

;(count (trackers @conn))
;(let [q '[:find [?e ...]
;          :where [?e :tracker/id]]]
;  (d/q q @conn))

;(def tr1 (first (trs @conn)))
;(:tracker/label tr1)

;(d/q '[:find ?id ?label
;       :where [?t :tracker/status_movement "parked"]
;              [?t :tracker/id ?id]
;              [?t :tracker/label ?label]]
;     @conn)
;
;(d/pull [@conn '*'])

;(identity conn)
;(d/reset-conn! conn @conn)

;(def zones [{:id "z0" :label "480 КЖИ - погр."}
;            {:id "z1" :label "Балашиха - разгр."}
;            {:id "z2" :label "Боброво - разгр."}
;            {:id "z3" :label "Бунинские луга - разгр."}
;            {:id "z4" :label "Ново-Куркино (Химки) - разгр."}])


;(def groups [{:id "g0" :title "-Инлоудер"}
;             {:id "g1" :title "-Лангендорф"}
;             {:id "g2" :title "-Панелевоз У-148 12,5т"}
;             {:id "g3" :title "Погрузчики и технологические"}])


;(def trackers [{:id "t0" :label "о230ес50 лангендорф Щелково" :status_movement "parked" :event_time "2017-10-19 16:10:00" :zone_label_current "480 КЖИ - погр."}
;               {:id "t1" :label "у901еу750 У-230 Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
;               {:id "t2" :label "с192во777 борт 20т 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
;               {:id "t3" :label "а446хв77 У-230 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}])
