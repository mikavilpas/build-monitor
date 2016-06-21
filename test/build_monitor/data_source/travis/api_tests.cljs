(ns build-monitor.data-source.travis.api-tests
  (:require [build-monitor.data-source.travis.api :as travis]
            [reagent.core :as reagent]
            [build-monitor.test-utils :as test-utils])
  (:require-macros [devcards.core :as dc :refer [defcard deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(def config (travis/Config. "sp3ctum" "evil-lispy"))

(defcard test-call
  (test-utils/channel->atom
   (travis/branches config)))
