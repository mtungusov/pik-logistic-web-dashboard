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

(defonce default-trackers-query '([?e :tracker/id]
                                  [?e :tracker/zone_label_current ?z]
                                  [?e :tracker/group_title ?g]))

(defn make-where [acc zone-set group-set]
  (if (empty? zone-set)
    (if (empty? group-set)
      acc
      (concat acc ['[(contains? ?groups ?g)]]))
    (make-where (concat acc ['[(contains? ?zones ?z)]]) #{} group-set)))



;(let [q {:find '[[(pull ?e [*]) ...]]
;         :in '[$ ?zones ?groups]
;         :where (make-where default-trackers-query z g)}]
;  (d/q q @conn z g))

(defn trackers [db zones-set groups-set]
  (let [q {:find '[[(pull ?e [*]) ...]]
           :in '[$ ?zones ?groups]
           :where (make-where default-trackers-query zones-set groups-set)}
        items (d/q q db zones-set groups-set)]
    (sort-by :tracker/order-comp items)))

;(first (trackers @conn #{}))


(defn groups [db]
  (let [q '[:find [(pull ?e [*]) ...] :in $ :where [?e :group/id]]
        items (d/q q db)]
    (sort-by :group/title items)))

;(groups @conn)


(defn zones [db]
  (let [q '[:find [(pull ?e [*]) ...] :in $ :where [?e :zone/id]]
        items (d/q q db)
        out-zone {:zone/id "z-0", :zone/label "вне зон"}]
    (conj (sort-by :zone/label items) out-zone)))

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
;(def z #{})
;(def z #{"480 КЖИ - погр."})
;(def g #{"-Инлоудер" "Легковой автомобиль"})
;(def g #{"Легковой автомобиль"})
;(empty? z)
;(d/q '[:find [(pull ?e [:tracker/id]) ...] :in $ ?z
;       :where [?e :tracker/id]
;              [?e :tracker/zone_label_current ?lz]
;              [(or(contains? ?z ?lz)true)]]
;      @conn z)



;(make-where default-tracker-query #{} #{"g1"})


;(let [q {:find '[[(pull ?e [:tracker/id]) ...]]
;         :in '[$ ?zones ?groups]
;         :where (concat ['[?e :tracker/id]]
;                        ['[?e :tracker/zone_label_current ?lz]]
;                        ['[(contains? ?z ?lz)]])}]
;  (identity q)
;  (d/q q @conn #{"480 КЖИ - погр."}))

;(let [q {:find '[[(pull ?e [*]) ...]]
;         :in '[$ ?zones ?groups]
;         :where (make-where default-tracker-query z g)}]
;  (d/q q @conn z g))

              ;[(contains? ?z ?lz)]]
;(contains? z "one")