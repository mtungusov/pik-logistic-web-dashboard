(ns pik-logistic-dashboard.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [pik-logistic-dashboard.subs :as subs]
            [pik-logistic-dashboard.events :as events]))


(defonce ui-state (r/atom {:geo-zones {:collapsed true}
                           :groups {:collapsed true}}))


(defn chkbox [label value db-key subs-key]
  (let [checked (rf/subscribe [subs-key value])]
    [:label {:key label
             :class "form-check-label"
             :style {:display :block}}
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


(defn selector-geo-zones []
  (let [items (rf/subscribe [::subs/geo-zones])]
    [:div.form-check.bg-light
     (doall
       (for [i @items]
         (chkbox i i :geo-zones-selected ::subs/geo-zone-selected?)))]))


(defn selector-groups []
  (let [items (rf/subscribe [::subs/groups])]
    [:div.form-check.bg-light
     (doall
       (for [i @items]
         (chkbox i i :groups-selected ::subs/group-selected?)))]))


(defn set-time-class [dur]
  (cond
    (>= dur (* 120 60)) "badge-danger"
    (>= dur (* 60 60)) "badge-warning"
    :else "badge-light"))


(defn show-tracker [t]
  [:div.row.tracker-info {:key (str (:tracker_id t))}
   [:div.col-3.tracker-label
    [:span {:class "badge badge-light"} (:tracker_plate t)]
    [:span (:tracker_desc t)]]
   [:div.col-2
    [:div
     [:span.badge {:class "badge-light"} (:movement_status_fmt t)]
     [:span.badge {:class (case (:connection_status_fmt t) "вкл." "badge-success" "badge-dark")} (:connection_status_fmt t)]]
    [:span.time-ago {:title (:gps_updated_fmt t) :data-toggle "tooltip" :data-placement "left"} (:gps_updated_ago t)]]
   [:div.col-3.zone-label [:span.badge {:class (when-not (:zone_label_in t) "badge-secondary")}
                           (when-not (:zone_label_in t) "Выезд: ") (:zone_label_inout t)]]
   [:div.col-2 [:span.badge {:class (set-time-class (:time_inout_duration t))} (:time_inout_duration_fmt t)]]
   [:div.col-2 [:span.badge {:class (if (:zone_label_in t) "badge-light" "badge-secondary")} (:time_inout_fmt t)]]])


(defn list-trackers-header []
  [:div#list-trackers-header.row
   [:div.col-3.align-self-center "Автомобиль"]
   [:div.col-2.align-self-center "Статус"]
   [:div.col-3.align-self-center "Геозона"]
   [:div.col-2.align-self-center "Время в зоне/вне зон"]
   [:div.col-2.align-self-center "Въезд/Выезд"]])

(defn list-trackers []
  (let [items (rf/subscribe [::subs/trackers-filtered-by-geo-zones-and-groups])]
    [:div#list-trackers
     (list-trackers-header)
     (for [i @items]
       (show-tracker i))]))


(defn tool-header [label ui-key icon]
  (let [collapsed (get-in @ui-state [ui-key :collapsed])]
    [:div.tool-header.bg-light {:class "text-primary"
                                :on-click (fn []
                                            (swap! ui-state update-in [ui-key :collapsed] not)
                                            (rf/dispatch [::events/selected->local-storage ui-key collapsed]))}
     [:span.oi {:class (str icon (when-not collapsed " d-none"))}]
     [:span {:class (when collapsed "d-none")} label]]))

(defn tool-buttons [bu-clear bu-invert]
  ; bu-* {:ev event-name :arg arg-for-event}
  [:div.btn-toolbar {:role "toolbar"}
   [:div.btn-group {:role "group"}
    [:button.btn.btn-sm.btn-outline-danger {:type "button"
                                            :on-click (fn []
                                                        (rf/dispatch [(:ev bu-clear) (:arg bu-clear)]))}
     [:span.oi.oi-ban]]
    [:button.btn.btn-sm.btn-outline-primary {:type "button"
                                             :on-click (fn []
                                                         (rf/dispatch [(:ev bu-invert)]))}
     [:span.oi.oi-loop-circular]]]])


(defn chkbox-block [label ui-key icon selector-comp bu-clear bu-invert]
  (let [collapsed (get-in @ui-state [ui-key :collapsed])]
    [:div.chkbox-block
     (tool-header label ui-key icon)
     [:div {:class (when collapsed "d-none")}
      (tool-buttons bu-clear bu-invert)
      (selector-comp)]]))


(defn show-selected [label subs-name]
  (let [items (rf/subscribe [subs-name])]
    [:div {:class (when (empty? @items) "d-none")}
     [:span {:class "text-info"} label]
     (clojure.string/join ", " (sort @items))]))


(defn status-bar []
  [:div#status-bar
   (show-selected "Геозоны:" ::subs/geo-zones-selected)
   (show-selected "Группы:" ::subs/groups-selected)])


(defn main-panel []
  [:div.row
   [:div.col-md-auto
    (chkbox-block "Геозоны" :geo-zones "oi-globe" selector-geo-zones {:ev ::events/clear-checkbox-selections
                                                                      :arg :geo-zones-selected}
                                                                     {:ev ::events/invert-geo-zones-selections})
    (chkbox-block "Группы транспорта" :groups "oi-grid-three-up" selector-groups {:ev ::events/clear-checkbox-selections
                                                                                  :arg :groups-selected}
                                                                                 {:ev ::events/invert-groups-selections})
    [:div.chkbox-block
     [:div.tool-header.bg-light {:class "text-primary"}
      [:a {:href "http://dashboard-cars.pik-industry.ru/charts/"
           :target "_blank"}
       [:span.oi.oi-bar-chart]]]]]
   [:div.col
    (status-bar)
    (list-trackers)]])

