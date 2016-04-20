(ns build-monitor.app
  (:require [build-monitor.build-monitor-loading :as monitor]
            [build-monitor.recent-events-loading :as recent-events]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn- app-component [state-atom
                      recent-events-state-atom]
  [:div.container-fluid
   [:div.row
    [:div.no-padding.col-xs-12.col-lg-9
     [monitor/build-monitor-component state-atom]]
    [:div.no-padding.col-xs-8.col-lg-3
     [recent-events/recent-events-component recent-events-state-atom]]]])

(defonce monitor-state-atom (reagent/atom nil))
(defonce recent-events-state-atom (reagent/atom nil))

(defn start-app [root-dom-node]
  (monitor/start-once! monitor-state-atom)
  (recent-events/start-once! recent-events-state-atom)
  (reagent/render [app-component
                   monitor-state-atom
                   recent-events-state-atom]
                  root-dom-node))
