(ns build-monitor.special-effects.backend-stuck-indicator-tests
  (:require [build-monitor.special-effects.backend-stuck-indicator :as backend-stuck-indicator]
            [cljs.test :refer-macros [async is]]
            [build-monitor.time-processing :as time-processing]
            [cljs-time.core :as time]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [defcard-rg deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(defcard-rg indicator
  [:div.vertical
   (str
    "In this example the threshold-seconds is set to "
    @backend-stuck-indicator/threshold-seconds
    "seconds. After that threshold is reached, the component should be displayed.")
   [:div.align-this-center
    [backend-stuck-indicator/stuck-indicator
     (time/minus @time-processing/current-time-atom
                 (time/seconds 29))]]])
