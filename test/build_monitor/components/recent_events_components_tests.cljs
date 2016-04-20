(ns build-monitor.components.recent-events-components-tests
  (:require [build-monitor.components.recent-events-components :as recent-events-components]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.utils :as utils]
            [cljs.core.async :as async :refer [<!]]
            [cljs.test :refer-macros [is]]
            [reagent.core :as reagent])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [devcards.core :as dc :refer [defcard-rg deftest]]))

(def include-me :yep)

(def test-events-atom
  (reagent/atom [{:event-type "Commit"
                  :triggered-by-user "Alpha Beta"
                  :time (utils/parse-date "2016-01-01T16:00:00+0200")
                  :additional-info-uri "http://example.com"
                  :labels ["Development branch"]
                  :explanation-message "Fix the clowncopters not copterizing"}
                 {:event-type "Team member tracking"
                  :triggered-by-user "Big brother - user tracking service"
                  :time (utils/parse-date "2016-01-01T16:30:00+0200")
                  :labels ["Master branch" "Merge"]
                  :explanation-message (str "The project owner has entered"
                                            " the premises. Act like you're doing hard work!")}
                 {:event-type "Commit"
                  :triggered-by-user "Charlie Delta"
                  :time (utils/parse-date "2016-01-01T17:00:00+0200")
                  :additional-info-uri "http://example.com"
                  :explanation-message "Change controllers to control more"}
                 {:event-type "BuildStarted"
                  :triggered-by-user "Alpha Beta"
                  :time (utils/parse-date "2016-01-01T18:00:00+0200")
                  :additional-info-uri "http://example.com"
                  :explanation-message "Build the branch 'DAADA'"}]))

(defcard-rg events-list
  (fn [state _]
    (js/console.log _)
    [recent-events-components/recent-events-list @state])
  test-events-atom
  {:inspect-data true})
