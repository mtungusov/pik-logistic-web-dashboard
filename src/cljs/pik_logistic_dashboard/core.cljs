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
  (dev-setup)
  (mount-root))
