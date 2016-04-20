(ns build-monitor.special-effects.heart-tests
  (:require [build-monitor.pubsub :as pubsub]
            [build-monitor.special-effects.heart :as heart])
  (:require-macros [devcards.core :as dc :refer [defcard-rg]]))

(def include-me :yep)

(defcard-rg heart
  [heart/heart-component])

(defn heart-with-fade-component []
  [:div
   [:input {:type "button"
            :value "click me"
            :on-click #(pubsub/publish {:topic :message/new-build-statuses-arrived})}]
   [heart/fading-heart-component :message/new-build-statuses-arrived]])

(defcard-rg heart-with-fade
  [heart-with-fade-component])
