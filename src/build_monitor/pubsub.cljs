(ns build-monitor.pubsub
  (:require [cljs.core.async :as async :refer [>! chan pub sub]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Provides a global message bus that can easily be subscribed to

;; push data here
(defonce ^:private publish-channel (chan))

;; subscribe here
(defonce ^:private publication (pub publish-channel
                                    (fn get-topic [msg]
                                      (:topic msg))))

(defn publish [msg]
  (go (>! publish-channel msg)))

(defn subscribe
  "Makes matching topics appear on output-channel.
  Returns the channel."

  ;; can subscribe to a single topic only
  ([topic]
   (subscribe topic (chan)))

  ;; can subscribe to multiple topics
  ([topic output-channel]
   (sub publication topic output-channel)))
