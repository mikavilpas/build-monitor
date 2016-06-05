(ns build-monitor.data-source.travis-client-tests
  (:require [build-monitor.data-source.travis-client :as client]
            [reagent.core :as reagent]
            [build-monitor.test-utils :as test-utils])
  (:require-macros [devcards.core :as dc :refer [defcard deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(defcard test-call
  (test-utils/channel->atom
   (client/call "/repos/sp3ctum/evil-lispy/branches")))
