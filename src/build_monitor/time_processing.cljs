(ns build-monitor.time-processing
  (:require [schema.core :as s]
            [build-monitor.types :as types]
            [cljs-time.core :as time]
            [reagent.core :as reagent]
            [cljs-time.format :as time-format]
            [cljs-time.coerce :as time-coerce]
            [cljs-time.local :as time-local]
            [cljsjs.moment]))

(s/defn duration [start :- types/LocalDateTime
                  end :- types/LocalDateTime]
  (try
    (let [difference (time/interval start end)]
      (str (time/in-minutes difference)
           " min"))
    (catch js/Error e
      "")))

(defn duration-in-seconds [start end]
  (-> (time/interval start end)
      time/in-seconds))

(def moment js/moment)

(s/defn human-readable-duration :- s/Str [some-time :- types/LocalDateTime
                                          & [current-time :- types/LocalDateTime]]
  (let [current-time (moment (or (time-coerce/to-date current-time)
                                 (js/Date.)))]
    (-> (moment (time-coerce/to-date some-time))
        (.from current-time true))))

(s/defn how-long-ago :- s/Str [some-time :- types/LocalDateTime
                               & [current-time :- types/LocalDateTime]]
  (str (human-readable-duration some-time current-time)
       " ago"))

(s/defn how-long-to :- s/Str [source-time :- time-local/ILocalCoerce
                              destination-time :- time-local/ILocalCoerce]
  (-> (moment (time-coerce/to-date destination-time))
      (.from (time-coerce/to-date source-time))))

(defonce human-readable-date-time-formatter "EEEE dd.MM.yyyy   HH:mm:ss")

(s/defn human-readable-time :- s/Str
  [local-date]
  (let [formatter (time-format/formatter human-readable-date-time-formatter)]
    (time-format/unparse formatter local-date)))

(s/defn date-string-to-local-date :- types/LocalDateTime
  [utc-date-string :- s/Str]
  (when utc-date-string
    (-> (js/Date. utc-date-string)
        time-coerce/to-date-time
        time/local-date-time)))

(s/defn now :- types/LocalDateTime []
  (time-local/local-now))

(def Interval [(s/one types/LocalDateTime "start")
               (s/one types/LocalDateTime "finish")])

(s/defn estimated-completion-time [last-completed-build :- Interval
                                   current-start-time :- time-local/ILocalCoerce]
  (let [last-build-length (-> (apply time/interval last-completed-build)
                              time/in-seconds
                              time/seconds)]
    (time/plus current-start-time
               last-build-length)))

(s/defn percentage-of-time :- s/Int
  [source :- Interval
   target :- Interval]
  (let [target-interval (apply time/interval source)
        source-interval (apply time/interval target)]
    (->> (/ (time/in-seconds source-interval)
            (time/in-seconds target-interval))
         (/ 100)
         int)))

(defonce current-time-atom
  (let [a (reagent/atom (now))]
    (js/window.setInterval #(reset! a (now)) 1000)
    a))
