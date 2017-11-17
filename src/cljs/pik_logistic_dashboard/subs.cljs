(ns pik-logistic-dashboard.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db _]
   (:name db)))

(rf/reg-sub
  ::groups
  (fn [db _]
    (sort (:groups db))))

(rf/reg-sub
  ::groups-selected
  (fn [db _]
    (:groups-selected db)))

(rf/reg-sub
  ::group-selected?
  (fn [_ _]
    (rf/subscribe [::groups-selected]))
  (fn [items [_ value]]
    (contains? items value)))

(rf/reg-sub
  ::geo-zones
  (fn [db _]
    (conj (sort (:geo-zones db)) "вне зон")))

(rf/reg-sub
  ::geo-zones-selected
  (fn [db _]
    (:geo-zones-selected db)))

(rf/reg-sub
  ::geo-zone-selected?
  (fn [_ _]
    (rf/subscribe [::geo-zones-selected]))
  (fn [items [_ value]]
    (contains? items value)))

(rf/reg-sub
  ::trackers
  (fn [db _]
    (let [trs (:trackers db)
          res (map first (vals (group-by :id trs)))]
      res)))


(defn filter-fn [key-v v filter-vals]
  (contains? filter-vals (key-v v)))

(defn apply-filter [key-item items filter-vals]
  (if (empty? filter-vals)
    items
    (filter #(filter-fn key-item % filter-vals) items)))

(rf/reg-sub
  ::trackers-filtered-by-geo-zones
  (fn [_ _]
    [(rf/subscribe [::trackers])
     (rf/subscribe [::geo-zones-selected])])
  (fn [[items filter-set]]
    (apply-filter :zone_label_current items filter-set)))


(rf/reg-sub
  ::trackers-filtered-by-geo-zones-and-groups
  (fn [_ _]
    [(rf/subscribe [::trackers-filtered-by-geo-zones])
     (rf/subscribe [::groups-selected])])
  (fn [[items filter-set]]
    (apply-filter :group_title items filter-set)))
