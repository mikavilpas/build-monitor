(ns build-monitor.pubsub-tests
  (:require [cljs.test :as test :refer-macros [is testing]]
            [build-monitor.pubsub :as pubsub]
            [cljs.core.async :as async :refer [chan <! >!]]
            [reagent.core :as reagent]
            [schema.core :as s])
  (:require-macros [devcards.core :as dc :refer [defcard-rg defcard deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

;; Looks like channels need to be defonce singletons. I tried using it
;; locally in tests, but on reloads it caused re-run tests to
;; timeout. It's possible when the channel is replaced, the old one is
;; closed.  Then the whole publication will block.
(defonce test-channel (pubsub/subscribe :pubsub-tests (chan)))
(defonce another-test-channel (pubsub/subscribe :pubsub-tests (chan)))

(deftest can-read-published-message-from-pubsub
  (test/async test-done
              (let [msg {:topic :pubsub-tests
                         :data [1 2 3]}]
                (go (pubsub/publish msg)
                    (is (= msg (<! test-channel)))
                    (is (= msg (<! another-test-channel)))
                    (test-done)))))
