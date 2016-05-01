(ns build-monitor.data-source.event-source-tests
  (:require [build-monitor.data-source.common :as common]
            [build-monitor.data-source.event-source :as event-source]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.types :as types]
            [build-monitor.utils :as utils]
            [reagent.core :as reagent]
            [cljs.core.async :as async :refer [<!]]
            [cljs.test :refer-macros [async is testing]]
            [schema.core :as s :include-macros true])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg]]))

(def include-me :yep)
(def test-config test-utils/test-config)
(def recent-events-response
  (test-utils/channel->atom (go (<! (common/get-body
                                     "/mock-data/recent-events.json")))))

(defcard-rg test-api-call-results
  recent-events-response)

(defcard parsed-events
  (map #'event-source/api-recent-event->recent-event
       (:Events @recent-events-response)))

(deftest parses-api-event-correctly
  (is (= {:event-type "Commit",
          :triggered-by-user "Alpha Beta",
          :time (utils/parse-date "2016-03-04T16:00:00+0200"),
          :additional-info-uri "http://example.com",
          :explanation-message "This is a commit message."
          :labels nil}
         (#'event-source/api-recent-event->recent-event
          {:EventType "Commit",
           :TriggeredByUser "Alpha Beta",
           :Time "2016-03-04T16:00:00+0200",
           :AdditionalInfoUri "http://example.com",
           :ExplanationMessage "This is a commit message."}))))
