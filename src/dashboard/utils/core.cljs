(ns dashboard.utils.core
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [goog.string :as gstring]
            [clojure.string :as string]))


(defn nil-to-str [str]
  (if (nil? str) "" str))


(defn to-sec [time-int]
  (js/Math.trunc (/ time-int 1000)))


(defn to-sec-from-str [time-str]
  (let [t (tf/parse-local time-str)]
    (to-sec t)))


(defn format-time [time-str]
  (let [t (tf/parse-local time-str)]
    (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm") t)))

;(format-time "2017-10-24 10:47:56")
;(require '[goog.string :as gstring])
;(require '[clojure.string :as string])
;(float 59)
;(gstring/format "%02d" 9)
;(gstring/padNumber 198.523456 2 0)
;(string/join " " [1 2 3])

(def one-min 60)
(def one-hour (* 60 one-min))
(def one-day (* 24 one-hour))

(defn format-sec [sec]
  (let [dd (/ sec one-day)
        hh (/ (rem sec one-day) one-hour)
        mm (/ (rem (rem sec one-day) one-hour) one-min)
        ss (rem (rem (rem sec one-day) one-hour) one-min)
        in-day (string/join ":" (map #(gstring/padNumber % 2 0) [hh mm]))]
    (if (>= dd 1)
      (str (gstring/padNumber dd 2 0) "д. " in-day)
      in-day)))

;(string/join ":" (map #(gstring/padNumber % 2 0) [12.345 34.5677]))
;(format-sec 12386400)

;(require '[cljs-time.coerce :as tc])
;(require '[cljs-time.format :as tf])
;(tc/from-long (tc/to-long (tf/parse-local "2017-10-19 15:48:00")))
;(tc/to-long (t/now))
;(t/now)
;(/ (tc/to-long (t/now)) 1000)
;
;(js/Date.)
;(t/now)
;(tc/from-long (tc/to-long "2017-10-19 15:21:00"))
;(t/to-utc-time-zone (tc/from-string "2017-10-19 15:21:00"))

(defn now-int []
  (tc/to-long (t/now)))