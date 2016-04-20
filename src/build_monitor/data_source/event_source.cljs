(ns build-monitor.data-source.event-source
  (:require [build-monitor.data-source.common :as common]
            [build-monitor.utils :as utils]
            [build-monitor.time-processing :as time-processing]
            [build-monitor.types :as types]
            [cljs.core.async :as async :refer [<!]]
            [clojure.string :as str]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(s/defrecord RecentEvent [event-type
                          triggered-by-user
                          time
                          additional-info-uri
                          explanation-message
                          labels :- (s/maybe [s/Str])])

(defn- api-recent-event->recent-event [api-recent-event]
  (let [get-key #(get api-recent-event %)]
    (map->RecentEvent {:event-type (get-key :EventType)
                       :triggered-by-user (get-key :TriggeredByUser)
                       :time (utils/parse-date (get-key :Time))
                       :additional-info-uri (get-key :AdditionalInfoUri)
                       :explanation-message (get-key :ExplanationMessage)
                       :labels (get-key :Labels)})))

(defn get-recent-events [url]
  (go (let [response (<! (common/get-body url))
            events (map api-recent-event->recent-event (:Events response))]
        {:recent-events events
         :update-time (utils/parse-date (:UpdateTime response))})))
