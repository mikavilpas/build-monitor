(ns build-monitor.recent-events-loading
  (:require [build-monitor.components.recent-events-components :as recent-events-components]
            [build-monitor.config :as config]
            [build-monitor.data-source.event-source :as event-source]
            [build-monitor.pubsub :as pubsub]
            [build-monitor.special-effects.backend-stuck-indicator :as backend-stuck-indicator]
            [build-monitor.special-effects.heart :as heart]
            [build-monitor.utils :as utils]
            [cljs.core.async :as async :refer [<!]]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn get-initial-state []
  {:recent-events []
   :last-updated-time nil
   :config {}})

(defn- new-events-arrived [recent-events state-atom]
  (swap! state-atom assoc :recent-events recent-events)
  (pubsub/publish {:topic :message/new-recent-events-arrived
                   :data recent-events}))

(defn- last-updated-time-changed [new-update-time state-atom]
  (swap! state-atom assoc :last-updated-time new-update-time))

(defn- start-getting-recent-events! [state-atom]
  (let [url (-> @state-atom :config :apis :recent-events)]
    (utils/repeated-timed-calls
     (-> @state-atom :config :build-status-poll-timeout-ms)
     (fn update-recent-events! []
       (go (let [{:keys [recent-events update-time]}
                 (<! (event-source/get-recent-events url))]
             (if (not-empty recent-events)
               (do (new-events-arrived recent-events state-atom)
                   (last-updated-time-changed update-time state-atom))
               (print "warning: received no recent-events from url:" url))))))))

(defn recent-events-component [state-atom]
  (let [last-updated-time-atom (reagent/cursor state-atom [:last-updated-time])]
    [:div.vertical
     [:div.recent-events-component
      [recent-events-components/recent-events-list (:recent-events @state-atom)]]
     [backend-stuck-indicator/stuck-indicator @last-updated-time-atom]
     [:div.align-this-center
      [heart/fading-heart-component :message/new-recent-events-arrived]]]))

(defonce started? (reagent/atom false))

(defn start-once! [state-atom]
  (when-not @started?
    (reset! started? true)
    (go (let [config (<! (config/read-config-from-url
                          ;; try to bypass browser cache
                          (str "production-config.yaml?v=" (rand-int 99999))))]
          (swap! state-atom assoc :config config)
          (start-getting-recent-events! state-atom)))
    state-atom))
