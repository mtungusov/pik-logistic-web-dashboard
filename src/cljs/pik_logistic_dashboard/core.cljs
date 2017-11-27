(ns pik-logistic-dashboard.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frisk.core :refer [enable-re-frisk!]]
            [pik-logistic-dashboard.events :as events]
            [pik-logistic-dashboard.views :as views]
            [pik-logistic-dashboard.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk! {:x 0 :y 0})
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [views/main-panel]
            (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/load-data])
  ;(rf/dispatch-sync [::events/local-storage->selected-zones])
  (dev-setup)
  (mount-root))

(defn ^:export periodicupdate []
  (js/setInterval #(rf/dispatch [::events/load-data]) 60000))

(defn ^:export loadsettings []
  (js/setTimeout #(rf/dispatch [::events/local-storage->selected-zones]) 10000)
  (js/setTimeout #(rf/dispatch [::events/local-storage->selected-groups]) 10000))
