(ns pik-logistic-dashboard.views
  (:require [re-frame.core :as rf]
            [pik-logistic-dashboard.subs :as subs]
            [pik-logistic-dashboard.events :as events]))


(defn chkbox [label value db-key subs-key]
  (let [checked (rf/subscribe [subs-key value])]
    [:label {:key label
             :class "form-check-label"}
     [:input {:type "checkbox"
              :class "form-check-input"
              :value value
              :checked @checked
              :on-click (fn [e]
                          (.preventDefault e)
                          (if @checked
                            (rf/dispatch [::events/uncheck-one-checkbox db-key value])
                            (rf/dispatch [::events/check-one-checkbox db-key value])))}]
     label]))


(defn selector-groups []
  (let [items (rf/subscribe [::subs/groups])]
    [:div {:style {:float :left}} "Groups:"
     (doall
       (for [i @items]
         (chkbox i i :groups-selected ::subs/group-selected?)))]))


(defn selector-geo-zones []
  (let [items (rf/subscribe [::subs/geo-zones])]
    [:div {:style {:float :left}} "Geo zones:"
     (doall
       (for [i @items]
         (chkbox i i :geo-zones-selected ::subs/geo-zone-selected?)))]))


(defn show-tracker [t]
  [:div {:key (str (:id t))
         :style {:border-bottom "dotted 1px black"}}
   [:div (:label t)]
   [:div
    [:div (:status_last_update t)]
    [:div (:status_connection t)]
    [:div (:status_movement t)]
    [:div (:status_gps_update t)]]
   [:div (:zone_label_current t)]
   [:div (:event_time t)]
   [:div (:event_time t)]])


(defn list-trackers []
  (let [items (rf/subscribe [::subs/trackers-filtered-by-geo-zones-and-groups])]
    [:div
     (for [i @items]
       (show-tracker i))]))


(defn main-panel []
  [:div
   (selector-groups)
   (selector-geo-zones)
   [:div {:style {:clear :both}}]
   (list-trackers)])

