(ns build-monitor.storage-tests
  (:require [build-monitor.special-effects.heart :as heart]
            [build-monitor.storage :as storage]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.utils :as utils]
            [cljs.core.async :as async :refer [<! >!]]
            [cljs.test :refer-macros [async is]]
            [clojure.string :as str]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-rg deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

;; this is so that a Storage can be shown in devcards's ui
(defmethod utils/convert-js-type js/Storage [s]
  (zipmap (.keys js/Object s)
          (map (fn [k] (.getItem s k))
               (.keys js/Object s))))

(defcard introduction
  "Storage is a layer for saving and retrieving data from the
   browser's localStorage using some implementation of
   accessing it.  The data must be reloadable even when the
   tab/browser is closed.  All data is local to the current
   domain only.")

(deftest save-and-retrieve-data
  (is (= 42
         (do (storage/save :some-value 42)
             (storage/get-value :some-value))))
  (is (= {:a 24}
         (do (storage/save :some-value {:a 24})
             (storage/get-value :some-value)))
      "can also deserialize data structures")
  (is (nil?
       (storage/get-value :non-existent-value))
      "doesn't crash when a value isn't found"))

(defcard-rg current-local-storage-contents
  [:div.vertical
   "this should update once per second, but keep in mind tests change it"
   [:button.btn.btn-default.align-this-center
    {:on-click (fn [] (storage/clear!))}
    "Click to clear local storage"]
   [test-utils/observe-value-with-timeout
    1000
    #(utils/convert-js-type js/localStorage)]])
