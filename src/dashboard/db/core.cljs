(ns dashboard.db.core
  (:require [datascript.core :as d]))

(def schema {:tracker/id {:db/unique :db.unique/identity}
             :tracker/label {:db/unique :db.unique/identity}
             :tracker/event_time {}
             :tracker/status_last_update_time {}
             :tracker/status_movement {}
             :tracker/status_connection {}
             :tracker/zone_label_current {:db/index true}
             :tracker/group_title {:db/index true}

             :zone/id {:db/unique :db.unique/identity}
             :zone/label {:db/index true}

             :group/id {:db/unique :db.unique/identity}
             :group/title {:db/index true}})

(defonce conn (d/create-conn schema))


(defn trackers [db]
  (let [q '[:find [(pull ?e [*]) ...] :in $ :where [?e :tracker/id]]
        items (d/q q db)]
    (sort-by :tracker/order items)))

;(trackers @conn)


(defn groups [db]
  (let [q '[:find [(pull ?e [*]) ...] :in $ :where [?e :group/id]]
        items (d/q q db)]
    (sort-by :group/title items)))

;(groups @conn)


(defn zones [db]
  (let [q '[:find [(pull ?e [*]) ...] :in $ :where [?e :zone/id]]
        items (d/q q db)]
    (sort-by :zone/label items)))

;(zones @conn)


;(d/q '[:find (pull ?e [*]) :in $ ?id :where [?e :tracker/id ?id]]
;     @conn "t-208160")

;(def r
;  (d/q '[:find [(pull ?e [*]) ...] :in $ ?id :where [?e :tracker/zone_label_current ?id]]
;     @conn "ВЗЖБК - разгр."))
;(identity r)
;(d/pull @conn '[*] 248)

;(let [rs (d/q '[:find ?e :in $ ?id :where [?e :tracker/id ?id]]
;              @conn "t-208160")]
;  (prn rs))
;  (d/pull @conn '[*] rs))

;(d/q '[:find ?e :in $ :where [?e :group/id _]] @conn)
;(sort-by :group/title (d/q '[:find [(pull ?e [*]) ...] :in $ :where [?e :group/id]] @conn))

;(d/q '[:find ?e :in $ :where [?e :zone/id]] @conn)
;(sort-by :zone/label (d/q '[:find [(pull ?e [*]) ...] :in $ :where [?e :zone/id]] @conn))
