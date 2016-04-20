(ns build-monitor.special-effects.spinner-tests
  (:require [build-monitor.special-effects.spinner :as spinner]
            [cljs.test :refer-macros [async is]]
            [cljs.core.async :as async :refer [<! >!]]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [defcard-rg deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(defcard-rg common-spinner
  [:div "Visit " [:a {:href "http://spin.js.org/"} "http://spin.js.org/"]
   " for more information and live examples."
   [spinner/spinner-component {}]])

(defcard-rg spinner-with-options
  (let [options (merge (spinner/scale 2)
                       (spinner/rounds-per-second 0.1))]
    [:div [spinner/spinner-component options]]))

(deftest converts-ms-delay-to-rounds-per-second
  (is (= {:speed 1} (spinner/delay-ms 1000)))
  (is (= {:speed 0.5} (spinner/delay-ms 2000)))
  (is (= {:speed 0.1} (spinner/delay-ms 10000)))
  (is (= {:speed 2} (spinner/delay-ms 500))))
