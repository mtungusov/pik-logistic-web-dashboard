(ns pik-logistic-dashboard.events
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [day8.re-frame.http-fx]
            [ajax.core :refer [json-request-format json-response-format]]
            [cljsjs.moment]
            [cljsjs.moment.locale.ru]
            [goog.string :as gstring]
            [pik-logistic-dashboard.db :as db]
            [pik-logistic-dashboard.subs :as subs]))

(def base-url "https://dashboard-cars.pik-industry.ru")
(def api-version "api/v4")
(def dashboard-time-format "DD.MM.YY HH:mm")


(defn uri [& path]
  (string/join "/" (concat [base-url api-version] path)))

;(uri "q/trackers")
;(js/moment 1511772780000)

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))


(rf/reg-event-fx
  ::load-data
  (fn [_ _]
    (rf/dispatch [::load-trackers])
    (rf/dispatch [::load-groups])
    (rf/dispatch [::load-geo-zones])))


(rf/reg-event-fx
  ::load-trackers
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/trackers")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::trackers-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-groups
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/groups")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::groups-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-geo-zones
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (uri "q/zones")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::zones-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::selected-zones->local-storage
  (fn [_ _]
    (let [items (rf/subscribe [::subs/geo-zones-selected])]
      (js/localStorage.setItem "selected-zones" (prn-str @items)))))

;(rf/dispatch [::selected-zones->local-storage])

(rf/reg-event-fx
  ::selected-groups->local-storage
  (fn [_ _]
    (let [items (rf/subscribe [::subs/groups-selected])]
      (js/localStorage.setItem "selected-groups" (prn-str @items)))))

;(rf/dispatch [::selected-groups->local-storage])


(rf/reg-event-fx
  ::selected->local-storage
  (fn [_ [_ sel-key collapsed]]
    (when-not collapsed
      (case sel-key
        :geo-zones (rf/dispatch [::selected-zones->local-storage])
        :groups (rf/dispatch [::selected-groups->local-storage])))))


(defn- validate-loaded-selected-zones [items]
  (let [zones (rf/subscribe [::subs/geo-zones])]
    (if-not (set? items)
      #{}
      (clojure.set/intersection items (set @zones)))))

;(validate-loaded-selected-zones #{"one" "480 КЖИ - погр."})


(defn- validate-loaded-selected-groups [items]
  (let [groups (rf/subscribe [::subs/groups])]
    (if-not (set? items)
      #{}
      (clojure.set/intersection items (set @groups)))))


(rf/reg-event-db
  ::local-storage->selected-zones
  (fn [db _]
    (let [items-str (js/localStorage.getItem "selected-zones")
          items (cljs.reader/read-string items-str)]
      (assoc db :geo-zones-selected (validate-loaded-selected-zones items)))))

;(rf/dispatch [::local-storage->selected-zones])

(rf/reg-event-db
  ::local-storage->selected-groups
  (fn [db _]
    (let [items-str (js/localStorage.getItem "selected-groups")
          items (cljs.reader/read-string items-str)]
      (assoc db :groups-selected (validate-loaded-selected-groups items)))))

;(rf/dispatch [::local-storage->selected-groups])


(.locale js/moment "ru")

(defn- format-time [time-utc]
  (when time-utc
    (let [t (js/moment time-utc)]
      (.format t dashboard-time-format))))

;(format-time nil)
;(js/moment nil)


(defn- gen-zone-label [tracker]
  (if-let [zone_label_in (:zone_label_in tracker)]
    zone_label_in
    (:zone_label_out tracker)))

(defn- gen-time-inout [tracker]
  (if-let [zone_label_in (:zone_label_in tracker)]
    (:time_in tracker)
    (:time_out tracker)))


(def one-min 60)
(def one-hour (* 60 one-min))
(def one-day (* 24 one-hour))

(defn format-sec [sec]
  (when sec
    (let [dd (/ sec one-day)
          hh (/ (rem sec one-day) one-hour)
          mm (/ (rem (rem sec one-day) one-hour) one-min)
          ss (rem (rem (rem sec one-day) one-hour) one-min)
          conv-f (comp #(gstring/padNumber % 2 0) js/Math.trunc)
          in-day (string/join ":" (map #(conv-f %) [hh mm]))]
      (if (>= dd 1)
        (str (gstring/padNumber dd 2 0) "д. " in-day)
        in-day))))


(defn- gen-duration [time-utc]
  (when time-utc
    (let [now (js/moment)
          t (js/moment time-utc)]
      (.diff now t "seconds"))))


(defn time-ago [time-utc]
  (.fromNow (js/moment time-utc)))


(defn parse-tracker-label [label]
  (let [r (re-find #"^(\S*)\s+(.*)$" label)]
    {:plate (nth r 1)
     :desc  (nth r 2)}))


(defn gen-status-movement [status]
  (case status
    "parked" "стоит"
    "stopped" "стоит"
    "moving" "движ."
    status))

(defn gen-status-connection [status]
  (case status
    "active" "вкл."
    "выкл."))


(defn- gen-order [tracker]
  (let [zone_label_in (:zone_label_in tracker)]
    (if (nil? zone_label_in)
      (str "яя:" (:time_out tracker))
      (str zone_label_in ":" (:time_in tracker)))))


(defn- convert-tracker [tracker]
  (let [zone_label_in (:zone_label_in tracker)
        zone_label_inout (gen-zone-label tracker)
        time_inout (gen-time-inout tracker)
        time_inout_duration (gen-duration time_inout)
        gps_updated (:gps_updated tracker)
        tracker-label-parsed (parse-tracker-label (:tracker_label tracker))]
    (cond-> tracker
      zone_label_in (assoc :zone_label_cur zone_label_in)
      (nil? zone_label_in) (assoc :zone_label_cur "вне зон")
      true (assoc :zone_label_inout zone_label_inout)
      true (assoc :time_inout time_inout)
      true (assoc :time_inout_fmt (format-time time_inout))
      true (assoc :time_inout_duration time_inout_duration :time_inout_duration_fmt (format-sec time_inout_duration))
      true (assoc :gps_updated_fmt (format-time gps_updated) :gps_updated_ago (time-ago gps_updated))
      true (assoc :tracker_plate (:plate tracker-label-parsed) :tracker_desc (:desc tracker-label-parsed))
      true (assoc :movement_status_fmt (gen-status-movement (:movement_status tracker)))
      true (assoc :connection_status_fmt (gen-status-connection (:connection_status tracker)))
      true (assoc :order-comp (gen-order tracker)))))


(rf/reg-event-db
  ::trackers-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      ;(js/console.log "loaded: " (str (extract-groups items)))
      ;(rf/dispatch [::load-groups items])
      ;(rf/dispatch [::load-geo-zones items])
      (assoc db :trackers (map convert-tracker items)))))


(rf/reg-event-db
  ::groups-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :groups (set items)))))

(rf/reg-event-db
  ::zones-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :geo-zones (set items)))))


(rf/reg-event-db
  ::error-api
  (fn [db _]
    (js/console.log "API error!")
    db))


(rf/reg-event-db
  ::check-one-checkbox
  (fn [db [_ db-key value]]
    (update db db-key conj value)))


(rf/reg-event-db
  ::uncheck-one-checkbox
  (fn [db [_ db-key value]]
    (update db db-key disj value)))


(rf/reg-event-db
  ::clear-checkbox-selections
  (fn [db [_ db-key]]
    (assoc db db-key #{})))


(rf/reg-event-db
  ::invert-geo-zones-selections
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/geo-zones]))]
      (update db :geo-zones-selected #(clojure.set/difference all-items %)))))

(rf/reg-event-db
  ::invert-groups-selections
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/groups]))]
      (update db :groups-selected #(clojure.set/difference all-items %)))))
