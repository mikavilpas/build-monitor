(ns build-monitor.build-monitor-loading
  (:require [build-monitor.components.build-monitor-components :as build-monitor-components]
            [build-monitor.components.config-components :as config-components]
            [build-monitor.config :as config]
            [build-monitor.data-source.common :as common]
            [build-monitor.data-source.data-source-v2 :as data-source]
            [build-monitor.pubsub :as pubsub]
            [build-monitor.special-effects.heart :as heart]
            [build-monitor.special-effects.backend-stuck-indicator :as backend-stuck-indicator]
            [build-monitor.utils :as utils]
            [cljs.core.async :as async :refer [<!]]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn- get-initial-state []
  {:builds []
   :last-updated-time nil
   :config {}})

(defn prepare-config! [state-atom config]
  (swap! state-atom assoc :config config)
  (let [config-atom (reagent/cursor state-atom [:config])]
    (config-components/merge-config! config-atom)))

(defn new-builds-arrived [new-builds state-atom]
  (pubsub/publish {:topic :message/new-build-statuses-arrived
                   :data new-builds})
  (swap! state-atom assoc :builds new-builds))

(defn- last-updated-time-changed [last-updated-time state-atom]
  (swap! state-atom assoc :last-updated-time last-updated-time))

(defn- start-getting-builds [state-atom]
  (let [config (:config @state-atom)
        url (-> config :apis :version2)
        data-source nil #_(data-source/->DataSourceForVersion2 url
                                                               (:common-errors config))
        ]
    (utils/repeated-timed-calls
     (:build-status-poll-timeout-ms config)
     (fn update-builds! []
       (go (let [{:keys [builds update-time]}
                 (<! (common/get-build-statuses data-source))]
             (if (not-empty builds)
               (do
                 (new-builds-arrived builds state-atom)
                 (last-updated-time-changed update-time state-atom))
               (print "warning: received builds " builds " and update-time " update-time))))))))

(defn build-monitor-component [state-atom]
  (let [config-atom (reagent/cursor state-atom [:config])
        last-updated-time-atom (reagent/cursor state-atom [:last-updated-time])]
    (fn []
      (let [available-build-names (into [] (map :build-name (:builds @state-atom)))]
        [:div.vertical
         [build-monitor-components/monitor-from-atom state-atom]
         [backend-stuck-indicator/stuck-indicator @last-updated-time-atom]
         [config-components/config-component config-atom available-build-names]
         [:div.align-this-center
          [heart/fading-heart-component
           :message/new-build-statuses-arrived]]]))))

(defonce started? (reagent/atom false))

(defn start-once! [monitor-state-atom]
  (when-not @started?
    (reset! started? true)
    (go (let [config (<! (config/read-config-from-url
                          ;; try to bypass browser cache
                          (str "production-config.yaml?v=" (rand-int 99999))))]
          (prepare-config! monitor-state-atom config)
          (start-getting-builds monitor-state-atom)))
    monitor-state-atom))
