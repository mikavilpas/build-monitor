(ns build-monitor.config-test
  (:require [build-monitor.config :as config]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.types :as types]
            [cljs.test :refer-macros [async is]]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<!]]
            [schema.core :as s])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-rg deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(def config-file-url "/test-config.yaml")

(defcard-rg read-config
  (let [input "key: value"]
    [:div (str "Reading '" input "'")
     [:pre
      (test-utils/pprint
       (#'config/safe-load input))]]))

(deftest read-config-test
  (async test-done
         (go (let [conf (<! (config/read-config-from-url config-file-url))]
               (is (s/validate types/Config conf))
               (test-done)))))

(defcard read-config-from-url-result
  (test-utils/channel->atom
   (go
     (<! (config/read-config-from-url config-file-url)))))
