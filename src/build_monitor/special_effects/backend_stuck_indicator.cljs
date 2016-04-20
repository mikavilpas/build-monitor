(ns build-monitor.special-effects.backend-stuck-indicator
  (:require [reagent.core :as reagent]
            [cljs-time.core :as time]
            [build-monitor.time-processing :as time-processing]))

(defonce ^:private threshold-seconds (reagent/atom 30))

(defn stuck-indicator [time]
  (try
    (let [now @time-processing/current-time-atom
          seconds-ago (time/in-seconds
                       (time/interval time now))]
      (when (> seconds-ago @threshold-seconds)
        [:span.label.label-danger
         "Backend unreachable "
         (time-processing/how-long-ago time now)]))
    (catch js/Error e
      ;; most errors are caused by time/interval - it has a
      ;; precondition stating the given times have to be in
      ;; order. Sometimes the server clock is ahead of the client
      ;; clock and this causes an error to be thrown:
      ;; Assert failed: (<= (.getTime start) (.getTime end))
      nil)))
