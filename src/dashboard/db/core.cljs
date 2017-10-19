(ns dashboard.db.core)

(def zones [{:id "z0" :label "480 КЖИ - погр."}
            {:id "z1" :label "Балашиха - разгр."}
            {:id "z2" :label "Боброво - разгр."}
            {:id "z3" :label "Бунинские луга - разгр."}
            {:id "z4" :label "Ново-Куркино (Химки) - разгр."}])


(def groups [{:id "g0" :title "-Инлоудер"}
             {:id "g1" :title "-Лангендорф"}
             {:id "g2" :title "-Панелевоз У-148 12,5т"}
             {:id "g3" :title "Погрузчики и технологические"}])


(def trackers [{:id "t0" :label "о230ес50 лангендорф Щелково" :status_movement "parked" :event_time "2017-10-19 16:10:00" :zone_label_current "480 КЖИ - погр."}
               {:id "t1" :label "у901еу750 У-230 Щелково" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
               {:id "t2" :label "с192во777 борт 20т 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}
               {:id "t3" :label "а446хв77 У-230 1АК" :status_movement "parked" :event_time "2017-10-11 10:38:19" :zone_label_current "480 КЖИ - погр."}])
