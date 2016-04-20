(ns build-monitor.special-effects.heart
  (:require [build-monitor.pubsub :as pubsub]
            [cljs.core.async :as async]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import [goog.fx.dom FadeIn FadeOut]))

(defn heart-component []
  [:div.heart
   [:span.glyphicon.glyphicon-heart]])

(defn fading-heart-component [toggling-pubsub-event]
  (let [new-builds-channel (pubsub/subscribe
                            :message/new-build-statuses-arrived)
        builds-listener (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [node (reagent/dom-node this)]
          (reset! builds-listener
                  (go-loop []
                    (async/<! new-builds-channel)
                    (-> (FadeIn. node 150) .play)
                    (async/<! (async/timeout 800))
                    (-> (FadeOut. node 150) .play)
                    (recur)))))

      :component-will-unmount #(async/close! @builds-listener)
      :reagent-render (constantly
                       [:div
                        {:style {:opacity "0"}}
                        [heart-component]])})))
